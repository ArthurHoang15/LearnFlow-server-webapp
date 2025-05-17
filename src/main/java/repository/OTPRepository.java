package repository;

import model.OTP.OTPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPEntity, Long> {

    /**
     * Tìm OTP mới nhất (theo thời gian hết hạn giảm dần) cho một email,
     * với điều kiện OTP đó vẫn còn hạn (thời gian hết hạn sau thời điểm hiện tại).
     *
     * @param email Email cần tìm OTP.
     * @param now   Thời điểm hiện tại để so sánh với expiryDate.
     * @return Optional chứa OTPEntity nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<OTPEntity> findFirstByEmailAndExpiryDateAfterOrderByExpiryDateDesc(String email, LocalDateTime now);

    /**
     * Xóa tất cả các OTP liên quan đến một địa chỉ email.
     * Thường được sử dụng trước khi tạo một OTP mới cho cùng một email
     * để đảm bảo chỉ có một OTP hợp lệ tại một thời điểm.
     *
     * @param email Email của các OTP cần xóa.
     */
    @Transactional // Cần thiết cho các hoạt động delete/update tùy chỉnh
    void deleteByEmail(String email);
}
