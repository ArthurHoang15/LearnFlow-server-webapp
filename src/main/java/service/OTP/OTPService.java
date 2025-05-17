package service.OTP;

import dto.OTP.OTPRequest; // DTO này chỉ cần email
import exception.Login.InvalidCredentialsException;
import exception.Login.ResourceNotFoundException;
import model.OTP.OTPEntity;
import model.OTP.OtpPurpose; // THÊM IMPORT
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.OTPRepository;
import repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OTPService {

    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);

    private final OTPRepository otpRepository;
    private final SenderService emailSenderService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    public OTPService(OTPRepository otpRepository,
                      @Qualifier("emailSenderService") SenderService emailSenderService,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.otpRepository = otpRepository;
        this.emailSenderService = emailSenderService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Phương thức cho OTP Đăng Ký ---
    @Transactional
    public void generateAndSendOTPForRegistration(String email) {
        logger.info("Attempting to generate REGISTRATION OTP for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found for REGISTRATION OTP generation: {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });

        if (user.isEnabled()) {
            logger.warn("Account for email {} is already enabled. REGISTRATION OTP generation aborted.", email);
            throw new IllegalStateException("Tài khoản này đã được kích hoạt.");
        }
        // Gọi hàm helper để tạo và gửi OTP
        generateAndSendOTP(email, OtpPurpose.REGISTRATION_VERIFICATION, user.getUsername());
    }

    @Transactional
    public boolean verifyOTPForRegistration(String email, String providedOtp) {
        logger.info("Attempting to verify REGISTRATION OTP for email: {}", email);
        return verifyOTP(email, providedOtp, OtpPurpose.REGISTRATION_VERIFICATION);
    }

    // --- Phương thức cho OTP Đặt Lại Mật Khẩu ---
    @Transactional
    public void generateAndSendOTPForPasswordReset(String email) {
        logger.info("Attempting to generate PASSWORD RESET OTP for email: {}", email);
        // Đối với quên mật khẩu, user phải tồn tại và đã được kích hoạt
        User user = userRepository.findByEmail(email)
                .filter(User::isEnabled) // Chỉ tìm user đã được kích hoạt
                .orElseThrow(() -> {
                    logger.warn("Active user not found for PASSWORD RESET OTP generation: {}", email);
                    // Không nên thông báo user không tồn tại vì lý do bảo mật,
                    // nhưng trong quá trình dev, có thể throw để dễ debug.
                    // Trong production, có thể chỉ log và không làm gì (hoặc gửi email chung chung nếu muốn)
                    return new ResourceNotFoundException("User", "email", email + " (not found or not active)");
                });
        // Gọi hàm helper để tạo và gửi OTP
        generateAndSendOTP(email, OtpPurpose.PASSWORD_RESET, user.getUsername());
    }

    @Transactional
    public boolean verifyOTPForPasswordReset(String email, String providedOtp) {
        logger.info("Attempting to verify PASSWORD RESET OTP for email: {}", email);
        return verifyOTP(email, providedOtp, OtpPurpose.PASSWORD_RESET);
    }


    // --- Hàm Helper Chung ---
    @Transactional // Đặt @Transactional ở đây để bao gồm cả xóa và lưu
    private void generateAndSendOTP(String email, OtpPurpose purpose, String username) {
        // Xóa OTP cũ cùng mục đích cho email này
        otpRepository.deleteByEmailAndPurpose(email, purpose);
        logger.debug("Old OTPs for email {} and purpose {} deleted.", email, purpose);

        String otpCode = generateRandomOTP();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        OTPEntity otpEntity = new OTPEntity(email, passwordEncoder.encode(otpCode), expiryDate, purpose);
        otpRepository.save(otpEntity);
        logger.info("OTP (purpose: {}) generated and saved for email: {}. Expires at: {}", purpose, email, expiryDate);

        String subject;
        String content;
        if (purpose == OtpPurpose.REGISTRATION_VERIFICATION) {
            subject = "LearnFlow - Mã Xác Thực Đăng Ký Tài Khoản";
            content = String.format(
                    "Chào bạn %s,\n\nCảm ơn bạn đã đăng ký tài khoản tại LearnFlow.\n" +
                            "Mã OTP để kích hoạt tài khoản của bạn là: %s\n" +
                            "Mã này sẽ có hiệu lực trong vòng %d phút.\n\n" +
                            "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.\n\n" +
                            "Trân trọng,\nĐội ngũ LearnFlow",
                    username, otpCode, otpExpiryMinutes
            );
        } else if (purpose == OtpPurpose.PASSWORD_RESET) {
            subject = "LearnFlow - Yêu Cầu Đặt Lại Mật Khẩu";
            content = String.format(
                    "Chào bạn %s,\n\nChúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản LearnFlow của bạn.\n" +
                            "Mã OTP của bạn là: %s\n" +
                            "Mã này sẽ có hiệu lực trong vòng %d phút.\n\n" +
                            "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và liên hệ với chúng tôi nếu bạn nghi ngờ có hoạt động bất thường.\n\n" +
                            "Trân trọng,\nĐội ngũ LearnFlow",
                    username, otpCode, otpExpiryMinutes
            );
        } else {
            logger.error("Unsupported OTP purpose: {}", purpose);
            throw new IllegalArgumentException("Unsupported OTP purpose.");
        }

        try {
            emailSenderService.send(email, subject, content);
            logger.info("OTP (purpose: {}) email sent to {}", purpose, email);
        } catch (Exception e) {
            logger.error("Failed to send OTP (purpose: {}) email to {}: {}", purpose, email, e.getMessage(), e);
            throw e; // Re-throw để transaction rollback
        }
    }

    @Transactional // Đặt @Transactional ở đây để bao gồm cả xóa OTP
    private boolean verifyOTP(String email, String providedOtp, OtpPurpose purpose) {
        Optional<OTPEntity> otpEntityOptional = otpRepository.findFirstByEmailAndPurposeAndExpiryDateAfterOrderByExpiryDateDesc(email, purpose, LocalDateTime.now());

        if (otpEntityOptional.isEmpty()) {
            logger.warn("No valid OTP found or OTP expired for email: {} and purpose: {}", email, purpose);
            return false;
        }

        OTPEntity otpEntity = otpEntityOptional.get();
        if (passwordEncoder.matches(providedOtp, otpEntity.getCode())) {
            logger.info("OTP (purpose: {}) verified successfully for email: {}", purpose, email);
            otpRepository.delete(otpEntity); // Xóa OTP đã sử dụng
            logger.debug("Used OTP for email {} and purpose {} deleted.", email, purpose);
            return true;
        } else {
            logger.warn("Invalid OTP provided for email: {} and purpose: {}", email, purpose);
            return false;
        }
    }

    private String generateRandomOTP() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}
