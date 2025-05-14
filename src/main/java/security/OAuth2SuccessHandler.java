package security;

import model.RefreshToken;
import model.User;
import repository.UserRepository;
import service.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauthUser.getAttributes();

            String email = (String) attributes.get("email");
            String googleId = (String) attributes.get("sub");

            User user = processUserAuthentication(attributes, email, googleId);

            // Tạo access token
            String accessToken = tokenProvider.generateTokenFromUsername(user.getUsername());

            // Lưu access token vào user (nếu cần)
            user.setAccessToken(accessToken);

            // Tạo refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            // Redirect với token
            String redirectUrl = "/oauth2/redirect?token=" + accessToken
                    + "&refreshToken=" + refreshToken.getToken();

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }

    private User processUserAuthentication(Map<String, Object> attributes, String email, String googleId) {
        // Tìm user theo googleId
        Optional<User> existingUserByGoogleId = userRepository.findByGoogleId(googleId);

        if (existingUserByGoogleId.isPresent()) {
            return existingUserByGoogleId.get();
        }

        // Tìm user theo email
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);

        if (existingUserByEmail.isPresent()) {
            User user = existingUserByEmail.get();
            user.setGoogleId(googleId);
            return user;
        }

        // Tạo user mới
        User newUser = new User();
        newUser.setGoogleId(googleId);
        newUser.setEmail(email);
        newUser.setUsername(generateUniqueUsername(email));
        newUser.setFirstName((String) attributes.get("given_name"));
        newUser.setLastName((String) attributes.get("family_name"));
        newUser.setPassword(UUID.randomUUID().toString());
        newUser.setPicture((String) attributes.get("picture"));
        newUser.setIsPublic(false);

        return userRepository.save(newUser);
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0];
        return baseUsername + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
