package respository;

import java.util.Optional;

import entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndEmail(String token, String email);
    void deleteByEmail(String email);
}
