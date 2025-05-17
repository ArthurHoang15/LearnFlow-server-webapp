package model.OTP;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes") // Giữ nguyên tên bảng hoặc đổi nếu muốn
@Getter
@Setter
@NoArgsConstructor
public class OTPEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code; // Mã OTP đã được HASH

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING) // Lưu trữ enum dưới dạng String
    @Column(nullable = false)
    private OtpPurpose purpose; // THÊM TRƯỜNG NÀY

    public OTPEntity(String email, String hashedCode, LocalDateTime expiryDate, OtpPurpose purpose) {
        this.email = email;
        this.code = hashedCode;
        this.expiryDate = expiryDate;
        this.purpose = purpose;
    }
}