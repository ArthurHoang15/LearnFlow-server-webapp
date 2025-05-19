package security;

import model.RefreshToken;
import model.User.ERole;
import model.User.Role;
import model.User.User;
import repository.UserRepository;
import service.RefreshTokenService;
import repository.RoleRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger; // THÊM IMPORT
import org.slf4j.LoggerFactory; // THÊM IMPORT
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class); // KHAI BÁO LOGGER

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauthUser.getAttributes();

            String email = (String) attributes.get("email");
            String googleId = (String) attributes.get("sub"); // "sub" là subject identifier của Google

            if (email == null || email.isEmpty()) {
                logger.error("Email not found in OAuth2 attributes for user with googleId: {}", googleId);
                // Xử lý lỗi, có thể redirect về trang lỗi hoặc trả về lỗi HTTP
                // Ví dụ: getRedirectStrategy().sendRedirect(request, response, "/oauth2/error?message=EmailNotFound");
                // Hoặc nếu đây là API call (ví dụ: mobile), thì có thể trả về lỗi JSON
                super.onAuthenticationSuccess(request, response, authentication); // Để Spring xử lý lỗi mặc định hoặc
                // throw new ServletException("Email not found from OAuth2 provider.");
                return;
            }

            User user = processUserAuthentication(attributes, email, googleId);

            // Tạo access token
            String accessToken = tokenProvider.generateTokenFromUsername(user.getUsername());

            // Tạo refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // Xây dựng URL redirect, bao gồm cả access token và refresh token
            // Frontend sẽ xử lý các token này từ URL
            String targetUrl = determineTargetUrl(request, response, authentication); // Lấy target URL mặc định hoặc từ request
            String redirectUrl = targetUrl + (targetUrl.contains("?") ? "&" : "?") + "token=" + accessToken
                    + "&refreshToken=" + refreshToken.getToken();

            if (response.isCommitted()) {
                logger.debug("Response has already been committed. Unable to redirect to " + redirectUrl);
                return;
            }
            clearAuthenticationAttributes(request); // Xóa các attribute tạm thời nếu có
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    private User processUserAuthentication(Map<String, Object> attributes, String email, String googleId) {
        Optional<User> existingUserByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingUserByGoogleId.isPresent()) {
            logger.info("User found by Google ID: {}. Logging in.", googleId);
            User user = existingUserByGoogleId.get();
            // Đảm bảo user được kích hoạt khi đăng nhập lại bằng Google
            if (!user.isEnabled()) {
                user.setEnabled(true);
                // userRepository.save(user); // Không bắt buộc với @Transactional
            }
            return user;
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            User userToUpdate = existingUserByEmail.get();
            logger.info("User found by email: {}. Linking Google ID: {} and ensuring account is enabled.", email, googleId);
            // Chỉ cập nhật googleId nếu nó chưa có
            if (userToUpdate.getGoogleId() == null || userToUpdate.getGoogleId().isEmpty()) {
                userToUpdate.setGoogleId(googleId);
            }
            // Đảm bảo user được kích hoạt nếu họ đăng nhập qua Google lần đầu (hoặc đã bị disable trước đó)
            if (!userToUpdate.isEnabled()) {
                userToUpdate.setEnabled(true);
            }
            // userRepository.save(userToUpdate); // Không bắt buộc với @Transactional
            return userToUpdate;
        }

        // Nếu không tìm thấy user bằng googleId hoặc email, tạo user mới
        logger.info("Creating new user for OAuth2 login. Email: {}, Google ID: {}", email, googleId);
        User newUser = new User();
        newUser.setGoogleId(googleId);
        newUser.setEmail(email);
        newUser.setUsername(generateUniqueUsernameFromEmail(email)); // Hàm tạo username duy nhất
        newUser.setFirstName((String) attributes.get("given_name"));
        newUser.setLastName((String) attributes.get("family_name"));
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Hash mật khẩu ngẫu nhiên
        newUser.setPicture((String) attributes.get("picture"));
        newUser.setIsPublic(false); // Hoặc giá trị mặc định bạn muốn
        newUser.setEnabled(true);   // User OAuth2 thường được kích hoạt ngay

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> {
                    logger.error("Default role ROLE_USER not found. Please initialize roles in the database.");
                    return new RuntimeException("Error: Default role ROLE_USER not found.");
                });
        newUser.getRoles().add(userRole);

        return userRepository.save(newUser);
    }

    // Phương thức này phải nằm ở cấp độ class
    private String generateUniqueUsernameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            // Tạo username ngẫu nhiên nếu email rỗng (trường hợp hiếm)
            return "user_" + UUID.randomUUID().toString().substring(0, 8);
        }
        String prefix = email.split("@")[0].replaceAll("[^a-zA-Z0-9_.-]", ""); // Loại bỏ ký tự đặc biệt
        if (prefix.isEmpty()) {
            prefix = "user";
        }
        String username = prefix;
        int count = 0;
        // Kiểm tra xem username đã tồn tại chưa, nếu có thì thêm suffix
        while (userRepository.existsByUsername(username)) {
            count++;
            username = prefix + count; // Ví dụ: johndoe1, johndoe2
        }
        return username;
    }
}
