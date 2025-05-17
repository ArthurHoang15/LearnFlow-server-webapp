package controller;

import dto.AuthDto;
import dto.OTPVerificationRequest; // THÊM IMPORT NÀY
import service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // THÊM LOGGER
import org.slf4j.LoggerFactory; // THÊM LOGGER
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Cân nhắc lại origin cho @CrossOrigin, có thể là "http://localhost:3000" nếu frontend chạy ở port 3000
// Hoặc cấu hình CORS toàn cục trong SecurityConfig sẽ tốt hơn.
// @CrossOrigin(origins = "http://localhost:3000") // Giữ nguyên hoặc cấu hình global
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class); // THÊM LOGGER
    private final AuthService authService;

    /**
     * Endpoint để người dùng đăng nhập.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest loginRequest) {
        logger.info("Received login request for user/email: {}", loginRequest.getUsernameOrEmail());
        AuthDto.LoginResponse loginResponse = authService.login(loginRequest);
        logger.info("Login successful for user/email: {}", loginRequest.getUsernameOrEmail());
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Endpoint để người dùng đăng ký tài khoản mới.
     * Sau khi gọi API này, người dùng sẽ nhận được OTP qua email để xác thực.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthDto.RegisterRequest registerRequest) {
        logger.info("Received registration request for email: {}", registerRequest.getEmail());
        // Gọi phương thức mới trong AuthService, phương thức này không trả về LoginResponse
        authService.registerAndSendVerificationOTP(registerRequest);
        logger.info("Registration process initiated for email: {}. OTP sent for verification.", registerRequest.getEmail());
        // Trả về thông báo cho người dùng biết cần kiểm tra email
        String successMessage = "Đăng ký thành công. Vui lòng kiểm tra email (" +
                registerRequest.getEmail() +
                ") để nhận mã OTP và hoàn tất xác thực tài khoản.";
        return ResponseEntity.status(HttpStatus.CREATED).body(successMessage);
    }

    /**
     * Endpoint để người dùng xác thực OTP sau khi đăng ký.
     * Nếu OTP hợp lệ, tài khoản sẽ được kích hoạt và người dùng được đăng nhập.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthDto.LoginResponse> verifyOtpAndActivateAccount(
            @Valid @RequestBody OTPVerificationRequest verificationRequest) {
        logger.info("Received OTP verification request for email: {}", verificationRequest.getEmail());
        AuthDto.LoginResponse loginResponse = authService.verifyOtpAndActivateAccount(
                verificationRequest.getEmail(),
                verificationRequest.getOtp()
        );
        logger.info("OTP verification successful and account activated for email: {}. User logged in.", verificationRequest.getEmail());
        return ResponseEntity.ok(loginResponse);
    }


    /**
     * Endpoint để xử lý redirect từ OAuth2 login (ví dụ: Google).
     * Frontend sẽ gọi endpoint này sau khi được redirect từ Google, mang theo token.
     * LƯU Ý: Logic xử lý token sau OAuth2 đã nằm trong OAuth2SuccessHandler.
     * Endpoint này chỉ mang tính chất thông báo hoặc có thể được dùng để frontend lấy token từ URL.
     * Với cách làm hiện tại, OAuth2SuccessHandler sẽ redirect trực tiếp về frontend với token trong URL.
     * Endpoint này có thể không cần thiết nếu OAuth2SuccessHandler redirect về frontend
     * và frontend tự xử lý token từ URL params.
     * Nếu bạn vẫn muốn giữ, hãy đảm bảo nó phù hợp với luồng OAuth2 của bạn.
     */
    @GetMapping("/oauth2/redirect")
    public ResponseEntity<String> oauthRedirectHandler(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String refreshToken,
            @RequestParam(required = false) String error) {

        if (error != null) {
            logger.error("OAuth2 authentication error: {}", error);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Lỗi xác thực OAuth2: " + error);
        }

        if (token != null && refreshToken != null) {
            logger.info("OAuth2 authentication successful. Tokens received.");
            // Thông thường, client (frontend) sẽ lấy token từ URL query params
            // và tự lưu trữ. Server không cần làm gì thêm ở endpoint này.
            // Nó chỉ là một điểm redirect.
            // Có thể trả về một trang HTML đơn giản hoặc một JSON thông báo.
            // Hoặc redirect về một trang cụ thể của frontend.
            return ResponseEntity.ok("Xác thực OAuth2 thành công. Client có thể lấy token từ URL.");
        }
        logger.warn("OAuth2 redirect called without tokens or with an error.");
        return ResponseEntity.badRequest().body("Thông tin token không hợp lệ hoặc thiếu.");
    }

    /**
     * Endpoint để refresh access token.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthDto.TokenRefreshResponse> refreshToken(@Valid @RequestBody AuthDto.TokenRefreshRequest request) {
        logger.info("Received request to refresh token.");
        AuthDto.TokenRefreshResponse response = authService.refreshToken(request);
        logger.info("Token refreshed successfully.");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để người dùng đăng xuất.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody AuthDto.LogoutRequest logoutRequest) {
        logger.info("Received logout request.");
        authService.logout(logoutRequest);
        logger.info("User logged out successfully.");
        return ResponseEntity.ok("Đăng xuất thành công.");
    }
}
