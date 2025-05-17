package service;

import dto.OTPRequest; // DTO này chỉ cần email
import exception.Login.InvalidCredentialsException;
import exception.Login.ResourceNotFoundException;
import model.OTP.OTPEntity;
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
    private final SenderService emailSenderService; // Sử dụng @Qualifier nếu có nhiều bean SenderService
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Dùng để hash OTP

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    public OTPService(OTPRepository otpRepository,
                      @Qualifier("emailSenderService") SenderService emailSenderService, // Tiêm cụ thể EmailSender
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.otpRepository = otpRepository;
        this.emailSenderService = emailSenderService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Tạo và gửi OTP cho mục đích xác thực đăng ký.
     * OTP chỉ được tạo nếu email thuộc về một user CHƯA KÍCH HOẠT.
     *
     * @param email Email của người dùng cần gửi OTP.
     * @throws ResourceNotFoundException Nếu không tìm thấy user với email cung cấp.
     * @throws IllegalStateException     Nếu tài khoản đã được kích hoạt.
     * @throws RuntimeException          Nếu có lỗi khi gửi email.
     */
    @Transactional // Đảm bảo các thao tác DB (delete, save) là atomic
    public void generateAndSendOTPForRegistration(String email) {
        logger.info("Attempting to generate OTP for registration for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found for email: {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });

        if (user.isEnabled()) {
            logger.warn("Account for email {} is already enabled. OTP generation aborted.", email);
            throw new IllegalStateException("Tài khoản này đã được kích hoạt.");
        }

        // Xóa các OTP cũ chưa hết hạn (hoặc tất cả OTP) của email này để đảm bảo tính duy nhất
        otpRepository.deleteByEmail(email);
        logger.debug("Old OTPs for email {} deleted.", email);

        String otpCode = generateRandomOTP();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        OTPEntity otpEntity = new OTPEntity();
        otpEntity.setEmail(email);
        otpEntity.setCode(passwordEncoder.encode(otpCode)); // HASH OTP trước khi lưu
        otpEntity.setExpiryDate(expiryDate);

        otpRepository.save(otpEntity);
        logger.info("OTP generated and saved for email: {}. OTP (hashed): {}, Expires at: {}", email, otpEntity.getCode(), expiryDate);

        // Gửi OTP (plain text) qua email
        String subject = "LearnFlow - Mã Xác Thực Đăng Ký Tài Khoản";
        String content = String.format(
                "Chào bạn %s,\n\nCảm ơn bạn đã đăng ký tài khoản tại LearnFlow.\n" +
                        "Mã OTP để kích hoạt tài khoản của bạn là: %s\n" +
                        "Mã này sẽ có hiệu lực trong vòng %d phút.\n\n" +
                        "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\nĐội ngũ LearnFlow",
                user.getUsername(), // Hoặc user.getFirstName() nếu có
                otpCode,
                otpExpiryMinutes
        );

        try {
            emailSenderService.send(email, subject, content);
            logger.info("OTP email sent to {}", email);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", email, e.getMessage(), e);
            // Cân nhắc: có nên throw exception ở đây để controller biết và rollback không?
            // Hiện tại, EmailSender đã throw RuntimeException nếu gửi lỗi, nên transaction sẽ rollback.
            throw e; // Re-throw để transaction rollback
        }
    }

    /**
     * Xác thực mã OTP được cung cấp cho một email.
     *
     * @param email       Email liên kết với OTP.
     * @param providedOtp Mã OTP người dùng nhập (chưa hash).
     * @return true nếu OTP hợp lệ và chưa hết hạn, ngược lại false.
     */
    @Transactional
    public boolean verifyOTPForRegistration(String email, String providedOtp) {
        logger.info("Attempting to verify OTP for email: {} with provided OTP: {}", email, "****"); // Không log OTP plain text

        // Tìm OTP mới nhất còn hạn cho email
        Optional<OTPEntity> otpEntityOptional = otpRepository.findFirstByEmailAndExpiryDateAfterOrderByExpiryDateDesc(email, LocalDateTime.now());

        if (otpEntityOptional.isEmpty()) {
            logger.warn("No valid OTP found or OTP expired for email: {}", email);
            return false; // Không có OTP hợp lệ hoặc OTP đã hết hạn
        }

        OTPEntity otpEntity = otpEntityOptional.get();

        // So sánh OTP người dùng nhập với mã OTP đã hash trong DB
        if (passwordEncoder.matches(providedOtp, otpEntity.getCode())) {
            logger.info("OTP verified successfully for email: {}", email);
            // OTP hợp lệ, xóa OTP đã sử dụng khỏi DB để tránh tái sử dụng
            otpRepository.delete(otpEntity);
            logger.debug("Used OTP for email {} deleted.", email);
            return true;
        } else {
            logger.warn("Invalid OTP provided for email: {}", email);
            // Có thể implement cơ chế đếm số lần thử sai ở đây
            return false; // OTP không khớp
        }
    }

    private String generateRandomOTP() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // Tạo số có 6 chữ số (từ 100000 đến 999999)
        return String.valueOf(number);
    }
}
