package model.Learning; // Hoặc model.Learning

import jakarta.persistence.*;
import lombok.*; // Thêm AllArgsConstructor, Builder
import model.User.User;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
// import model.User.User; // Điều chỉnh import cho User
 import model.Learning.Lesson; // Điều chỉnh import cho Lesson

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Thêm
@Builder        // THÊM
public class LessonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "correct_answers_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int correctAnswersCount = 0;

    @Column(name = "incorrect_answers_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int incorrectAnswersCount = 0;

    @Column(name = "total_questions_attempted", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int totalQuestionsAttempted = 0;

    @Column(name = "time_spent_seconds", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long timeSpentSeconds = 0;

    @Column(nullable = false, columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    private double score = 0.0;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean completed = false;

    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
