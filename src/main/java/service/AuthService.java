// package service;
package service;

import dto.AuthDto;
import exception.Login.ResourceNotFoundException;
import exception.Login.TokenRefreshException;
import exception.Login.UserAlreadyExistsException;
import model.RefreshToken;
import model.User;
import repository.RefreshTokenRepository;
import repository.UserRepository;
import security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    // Thời gian sống refresh token (có thể lấy từ application.properties)
    private final long refreshTokenDurationMs = 7 * 24 * 60 * 60 * 1000L; // 7 ngày

    public AuthDto.LoginResponse login(AuthDto.LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsernameOrEmail(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username or email", loginRequest.getUsernameOrEmail()));

        // Tạo refresh token mới
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthDto.LoginResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public AuthDto.LoginResponse register(AuthDto.RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .isPublic(false)
                .build();

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.generateToken(authentication);

        // Tạo refresh token cho user mới
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthDto.LoginResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    public AuthDto.TokenRefreshResponse refreshToken(AuthDto.TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() ->
                        new TokenRefreshException("Refresh token không tồn tại: " + request.getRefreshToken()));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        String newAccessToken = tokenProvider.generateTokenFromUsername(
                refreshToken.getUser().getUsername());

        return new AuthDto.TokenRefreshResponse(newAccessToken,
                refreshToken.getToken(),
                "Bearer");
    }

    public void logout(AuthDto.LogoutRequest request) {
        // Xoá refresh token khỏi database
        refreshTokenRepository.deleteByToken(request.getRefreshToken());
        // (tuỳ chọn) xoá accessToken lưu trong user nếu bạn có trường đó
    }

    private RefreshToken createRefreshToken(User user) {
        // Xoá hết token cũ của user (nếu muốn mỗi user chỉ có 1 token)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }
}
