package security;

import model.User;
import repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauthUser.getAttributes();

            String email = (String) attributes.get("email");
            String googleId = (String) attributes.get("sub");

            Optional<User> existingUser = userRepository.findByGoogleId(googleId);

            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                Optional<User> userByEmail = userRepository.findByEmail(email);
                if (userByEmail.isPresent()) {
                    // Link Google account to existing email account
                    user = userByEmail.get();
                    user.setGoogleId(googleId);
                    userRepository.save(user);
                } else {
                    // Create new user
                    user = new User();
                    user.setGoogleId(googleId);
                    user.setEmail(email);
                    user.setUsername(email.split("@")[0] + "-" + UUID.randomUUID().toString().substring(0, 8));
                    user.setFirstName((String) attributes.get("given_name"));
                    user.setLastName((String) attributes.get("family_name"));
                    user.setPassword(UUID.randomUUID().toString());
                    user.setPicture((String) attributes.get("picture"));
                    user.setIsPublic(false);
                    userRepository.save(user);
                }
            }

            // Generate JWT token
            String token = tokenProvider.generateTokenFromUsername(user.getUsername());

            // Update access token
            user.setAccessToken(token);
            userRepository.save(user);

            // Redirect with token
            String redirectUrl = "/oauth2/redirect?token=" + token;
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }
}