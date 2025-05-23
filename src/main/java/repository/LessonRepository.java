package repository;

import model.Learning.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Thêm nếu sau này cần lọc động
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {
    // Hiện tại, các phương thức mặc định của JpaRepository (như findAll) là đủ.
    // Bạn có thể thêm các phương thức query tùy chỉnh ở đây nếu cần trong tương lai.
    // Ví dụ: List<Lesson> findByFormat(String format);
}
