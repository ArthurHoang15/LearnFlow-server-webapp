package repository;

import model.Learning.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByLessonId(Long lessonId);

    // Nếu có trường orderInLesson, có thể thêm phương thức sắp xếp
     List<Question> findByLessonIdOrderByOrderInLessonAsc(Long lessonId);
}
