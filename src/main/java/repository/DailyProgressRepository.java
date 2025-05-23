package repository;

import entity.DailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyProgressRepository extends JpaRepository<DailyProgress, Integer> {
    Optional<DailyProgress> findByUserId(Integer userId);
}
