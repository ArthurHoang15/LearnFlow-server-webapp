package service;

import dto.AuthDto;
import exception.InvalidCredentialsException;
import exception.ResourceNotFoundException;
import exception.UserAlreadyExistsException;
import model.User;
import repository.UserRepository;
import security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthDto.LoginResponse login(AuthDto.LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT token
        String token = tokenProvider.generateToken(authentication);

        // Lấy thông tin người dùng
        User user = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(),
                        loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username or email",
                        loginRequest.getUsernameOrEmail()));

        // Cập nhật access token
        user.setAccessToken(token);
        userRepository.save(user);

        return new AuthDto.LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public AuthDto.LoginResponse register(AuthDto.RegisterRequest registerRequest) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        // Tạo user mới
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .isPublic(false)
                .build();

        userRepository.save(user);

        // Đăng nhập tự động sau khi đăng ký
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        // Cập nhật access token
        user.setAccessToken(token);
        userRepository.save(user);

        return new AuthDto.LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    // Triển khai thêm nếu cần xử lý đăng nhập bằng Google thủ công
    // Phương thức này chỉ cần thiết nếu bạn có API endpoint riêng cho việc này
    public AuthDto.LoginResponse processGoogleLogin(AuthDto.GoogleLoginRequest googleLoginRequest) {
        // Xử lý token Google ở đây (thường được xử lý bởi OAuth2SuccessHandler)
        // Đây là một phương thức giả định nếu bạn muốn xử lý Google token thủ công

        // Trong thực tế, việc này được xử lý bởi Spring OAuth2
        throw new UnsupportedOperationException("Google authentication through API is not supported, use OAuth2 flow instead");
    }
}
