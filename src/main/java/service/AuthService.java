package service;


import entity.PasswordResetToken;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import respository.PasswordResetTokenRepository;
import respository.UserRepository;

import java.util.Random;
import java.time.LocalDateTime;
import java.util.Random;
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public void sendOtpForPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete any existing token for this email
        tokenRepository.deleteByEmail(email);

        // Generate OTP
        String otp = generateOtp();
        // Set expiry to 15 minutes from now
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        // Save token
        PasswordResetToken token = new PasswordResetToken(otp, email, expiryDate);
        tokenRepository.save(token);

        // Send OTP via email
        emailService.sendOtpEmail(email, otp);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find token
        PasswordResetToken token = tokenRepository.findByTokenAndEmail(otp, email)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        // Check if token is expired
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        // Check if new password is same as old
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password cannot be the same as old password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token
        tokenRepository.deleteByEmail(email);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
