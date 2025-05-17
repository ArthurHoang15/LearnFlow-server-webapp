package model.OTP;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Getter
@Setter
@NoArgsConstructor // Lombok sẽ tạo constructor không tham số
public class OTPEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email; // Email của người dùng liên kết với OTP này

    @Column(nullable = false)
    private String code; // Mã OTP đã được HASH

    @Column(nullable = false)
    private LocalDateTime expiryDate; // Thời gian OTP hết hạn

    public OTPEntity(String email, String hashedCode, LocalDateTime expiryDate) {
        this.email = email;
        this.code = hashedCode;
        this.expiryDate = expiryDate;
    }
}
