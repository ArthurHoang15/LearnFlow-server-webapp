package repository;

import model.OTP.OTPEntity;
import model.OTP.OtpPurpose; // THÊM IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTPEntity, Long> {

    Optional<OTPEntity> findFirstByEmailAndPurposeAndExpiryDateAfterOrderByExpiryDateDesc(String email, OtpPurpose purpose, LocalDateTime now);

    @Transactional
    void deleteByEmailAndPurpose(String email, OtpPurpose purpose); // Xóa OTP theo email và mục đích

    // Giữ lại deleteByEmail nếu bạn muốn xóa tất cả OTP của 1 email bất kể mục đích
     @Transactional
     void deleteByEmail(String email);
}
