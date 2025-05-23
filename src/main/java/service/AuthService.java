package service;

import dto.AuthDto;
import exception.Login.InvalidCredentialsException;
import exception.Login.ResourceNotFoundException;
import exception.Login.TokenRefreshException;
import exception.Login.UserAlreadyExistsException;
import model.RefreshToken;
import model.User.User;
// import repository.RefreshTokenRepository; // KHÔNG CẦN NỮA, SẼ DÙNG RefreshTokenService
import repository.UserRepository;
import security.JwtTokenProvider;
import security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value; // Không cần nếu RefreshTokenService quản lý thời gian hết hạn
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.OTP.OTPService;
import dto.PassReset.ResetPasswordRequest;
import dto.PassReset.ForgotPasswordRequest;
import service.OTP.SenderService;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import model.User.ERole;
import model.User.Role;
import repository.RoleRepository;

// import java.time.Instant; // Không cần trực tiếp ở đây nữa
// import java.util.UUID; // Không cần trực tiếp ở đây nữa

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService; // TIÊM RefreshTokenService
    private final OTPService otpService;
    private final CustomUserDetailsService customUserDetailsService;
    private final SenderService emailSenderService;
    private final RoleRepository roleRepository;

    // Thời gian sống refresh token sẽ được quản lý bởi RefreshTokenService
    // private final long refreshTokenDurationMs = 7 * 24 * 60 * 60 * 1000L; // BỎ ĐI

    public AuthDto.LoginResponse login(AuthDto.LoginRequest loginRequest) {
        logger.info("Attempting login for user/email: {}", loginRequest.getUsernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsernameOrEmail(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> {
                    logger.warn("User not found during login for: {}", loginRequest.getUsernameOrEmail());
                    return new ResourceNotFoundException("User", "username or email", loginRequest.getUsernameOrEmail());
                });

        if (!user.isEnabled()) {
            logger.warn("Login attempt for disabled account: {}. OTP verification required.", loginRequest.getUsernameOrEmail());
            throw new InvalidCredentialsException("Tài khoản của bạn chưa được kích hoạt. Vui lòng kiểm tra email ("+ user.getEmail() +") để xác thực bằng mã OTP.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("User {} authenticated successfully.", user.getUsername());

        String accessToken = tokenProvider.generateToken(authentication);
        logger.debug("Access token generated for user {}.", user.getUsername());

        // SỬ DỤNG RefreshTokenService ĐỂ TẠO REFRESH TOKEN
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        logger.debug("Refresh token created/updated for user {}.", user.getUsername());

        return new AuthDto.LoginResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    @Transactional
    public void registerAndSendVerificationOTP(AuthDto.RegisterRequest registerRequest) {
        logger.info("Attempting registration for email: {}", registerRequest.getEmail());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Registration failed: Username {} already exists.", registerRequest.getUsername());
            throw new UserAlreadyExistsException("Username '" + registerRequest.getUsername() + "' đã tồn tại.");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: Email {} already exists.", registerRequest.getEmail());
            throw new UserAlreadyExistsException("Email '" + registerRequest.getEmail() + "' đã được sử dụng.");
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .isPublic(false)
                .enabled(false)
                .build();

        // --- GÁN ROLE MẶC ĐỊNH ---
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found. Please ensure it's initialized in the database."));
        user.getRoles().add(userRole);

        userRepository.save(user);
        logger.info("User {} registered successfully with role {} and enabled=false.", user.getUsername(), ERole.ROLE_USER);

        try {
            otpService.generateAndSendOTPForRegistration(registerRequest.getEmail());
            logger.info("OTP sent for email verification to {}.", registerRequest.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send OTP for email {}: {}. User registration might be rolled back due to @Transactional.", registerRequest.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public AuthDto.LoginResponse verifyOtpAndActivateAccount(String email, String otp) {
        logger.info("Attempting to verify OTP and activate account for email: {}", email);

        if (!otpService.verifyOTPForRegistration(email, otp)) {
            logger.warn("OTP verification failed for email: {}. Invalid or expired OTP.", email);
            throw new InvalidCredentialsException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
        logger.info("OTP verified successfully for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email {} during OTP activation. This should not happen if OTP was generated correctly.", email);
                    return new ResourceNotFoundException("User", "email", email);
                });

        if (user.isEnabled()) {
            logger.warn("Account for email {} is already enabled. Activation skipped. Proceeding to create login response.", email);
        } else {
            user.setEnabled(true);
            userRepository.save(user);
            logger.info("Account for email {} has been activated successfully.", email);
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("User {} authenticated after OTP verification/account activation.", user.getUsername());

        String accessToken = tokenProvider.generateToken(authentication);
        logger.debug("Access token generated for user {}.", user.getUsername());

        // SỬ DỤNG RefreshTokenService ĐỂ TẠO REFRESH TOKEN
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        logger.debug("Refresh token created/updated for user {}.", user.getUsername());

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
        String requestRefreshTokenString = request.getRefreshToken();
        logger.info("Attempting to refresh token using refresh token: {}...", requestRefreshTokenString.substring(0, Math.min(requestRefreshTokenString.length(), 10)));

        // SỬ DỤNG RefreshTokenService ĐỂ TÌM VÀ XÁC MINH REFRESH TOKEN
        return refreshTokenService.findByToken(requestRefreshTokenString)
                .map(refreshTokenService::verifyExpiration) // Kiểm tra token hợp lệ và chưa hết hạn
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    String newAccessToken = tokenProvider.generateToken(authentication);
                    logger.info("Access token refreshed successfully for user: {}", user.getUsername());
                    return new AuthDto.TokenRefreshResponse(newAccessToken, requestRefreshTokenString, "Bearer");
                })
                .orElseThrow(() -> {
                    logger.warn("Refresh token not found or invalid: {}...", requestRefreshTokenString.substring(0, Math.min(requestRefreshTokenString.length(), 10)));
                    return new TokenRefreshException(requestRefreshTokenString, "Refresh token không tồn tại hoặc không hợp lệ!");
                });
    }

    @Transactional
    public void logout(AuthDto.LogoutRequest request) {
        String refreshTokenString = request.getRefreshToken();
        logger.info("Attempting to logout user with refresh token: {}...", refreshTokenString.substring(0, Math.min(refreshTokenString.length(), 10)));

        // SỬ DỤNG RefreshTokenService ĐỂ REVOKE TOKEN
        // Giả sử RefreshTokenService có phương thức revokeRefreshToken(String tokenString)
        // Hoặc bạn có thể tìm token entity trước rồi mới revoke.
        // Để đơn giản, nếu RefreshTokenService có revokeToken(String token) thì dùng nó.
        // Nếu không, bạn có thể cần sửa RefreshTokenService hoặc làm như sau:
        /*
        refreshTokenService.findByToken(refreshTokenString.trim())
            .ifPresent(tokenEntity -> refreshTokenService.revokeToken(tokenEntity.getToken())); // Giả sử revokeToken nhận String
        */
        // Hiện tại, RefreshTokenService bạn cung cấp có revokeRefreshToken(String token)
        // nhưng logic của nó là deleteByToken. Điều này là ổn.
        refreshTokenService.revokeRefreshToken(refreshTokenString.trim());

        SecurityContextHolder.clearContext();
        logger.info("User logged out successfully. Refresh token (if existed) revoked/deleted.");
    }

    // BỎ PHƯƠNG THỨC private createOrUpdateRefreshToken(User user)
    // VÌ LOGIC NÀY GIỜ NẰM TRONG RefreshTokenService.createRefreshToken(userId)
    /*
    private RefreshToken createOrUpdateRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }
    */
    /**
     * Xử lý yêu cầu đặt lại mật khẩu.
     * Gửi OTP đặt lại mật khẩu đến email người dùng nếu email tồn tại và tài khoản đã kích hoạt.
     */
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        logger.info("Password reset requested for email: {}", request.getEmail());
        // OTPService.generateAndSendOTPForPasswordReset sẽ kiểm tra user tồn tại và đã enabled
        otpService.generateAndSendOTPForPasswordReset(request.getEmail());
        // Controller sẽ trả về thông báo cho người dùng
    }

    /**
     * Xác thực OTP và đặt lại mật khẩu mới cho người dùng.
     */
    @Transactional
    public void verifyOtpAndResetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String newPassword = request.getNewPassword();
        String confirmNewPassword = request.getConfirmNewPassword();

        logger.info("Attempting to reset password for email: {}", email);

        if (!newPassword.equals(confirmNewPassword)) {
            logger.warn("New password and confirm password do not match for email: {}", email);
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }

        if (!otpService.verifyOTPForPasswordReset(email, otp)) { // Giả sử OTPService có phương thức này
            logger.warn("Password reset OTP verification failed for email: {}. Invalid or expired OTP.", email);
            throw new InvalidCredentialsException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
        logger.info("Password reset OTP verified successfully for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email {} during password reset. This should not happen if OTP was for a valid user.", email);
                    return new ResourceNotFoundException("User", "email", email);
                });

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            logger.warn("New password is the same as the old password for email: {}", email);
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        // userRepository.save(user); // Không bắt buộc nếu @Transactional và user là managed entity
        logger.info("Password reset successfully for email: {}.", email);

        // Gửi email thông báo mật khẩu đã được thay đổi thành công
        try {
            String subject = "LearnFlow - Mật Khẩu Đã Được Thay Đổi";
            // Sử dụng DateTimeFormatter để định dạng ngày giờ
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            String formattedDateTime = LocalDateTime.now().format(formatter);
            String content = String.format(
                    "Chào bạn %s,\n\nMật khẩu cho tài khoản LearnFlow của bạn đã được thay đổi thành công vào lúc %s.\n\n" +
                            "Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với chúng tôi ngay lập tức.\n\n" +
                            "Trân trọng,\nĐội ngũ LearnFlow",
                    user.getUsername(), // Hoặc user.getFirstName() nếu có
                    formattedDateTime // Sử dụng ngày giờ đã định dạng
            );
            emailSenderService.send(email, subject, content); // Giả sử emailSenderService đã được tiêm
            logger.info("Password change confirmation email sent to {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password change confirmation email to {}: {}", email, e.getMessage(), e);
            // KHÔNG throw e ở đây để tránh rollback việc đổi mật khẩu
        }
    }
}
