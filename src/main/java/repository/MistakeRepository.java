package repository;

import model.Learning.Mistake;
import model.User.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MistakeRepository extends JpaRepository<Mistake, Long>, JpaSpecificationExecutor<Mistake> {

    @Query("SELECT m FROM Mistake m WHERE m.user = :user AND (:lessonId IS NULL OR m.question.lesson.id = :lessonId)")
    Page<Mistake> findMistakesByUserWithLessonFilter(@Param("user") User user, @Param("lessonId") Long lessonId, Pageable pageable);

    // Hoặc một phiên bản đơn giản hơn nếu bạn muốn xử lý lọc lessonId ở tầng service
    // Page<Mistake> findByUser(User user, Pageable pageable);
}
