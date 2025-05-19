package service.OTP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; // Thêm logger
import org.slf4j.LoggerFactory; // Thêm logger
import lombok.*;

@Service("emailSenderService") // Đặt tên cho bean để phân biệt nếu có nhiều implementation SenderService
@RequiredArgsConstructor
public class EmailSender implements SenderService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class); // Logger

    private final JavaMailSender mailSender;

    @Override
    public void send(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);


            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Error sending email to {}: {}", to, e.getMessage());
            // Bạn có thể ném một custom exception ở đây nếu muốn xử lý cụ thể hơn ở tầng gọi
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
