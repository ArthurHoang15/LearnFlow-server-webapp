package repository;

import model.Learning.Question; // Hoặc model.Question nếu Question.java ở đó
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByLessonId(Long lessonId);
    List<Question> findByLessonIdOrderByOrderInLessonAsc(Long lessonId); // Nếu có orderInLesson
}
