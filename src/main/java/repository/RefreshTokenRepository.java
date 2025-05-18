package repository;

import model.RefreshToken;
import model.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Transactional
    int deleteByUser(User user);

    @Transactional
    int deleteByToken(String token);
}
