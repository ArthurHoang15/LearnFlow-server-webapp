package repository;

import model.Learning.LessonProgress; // Hoặc model.Learning.LessonProgress
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<LessonProgress> findByUserIdAndLessonIdIn(Long userId, List<Long> lessonIds); // Giữ lại nếu LearningService.getAllLessons cần
}
