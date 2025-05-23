package model.Learning;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List; // Cho các options của multiple choice
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String content; // Nội dung câu hỏi/câu cần điền

    @Enumerated(EnumType.STRING) // LƯU TRỮ DƯỚI DẠNG STRING TRONG DB
    @Column(nullable = false)
    private QuestionType type;

    private String hint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    // Cho fill-in-the-blank: Đáp án đúng
    @Column(name = "correct_answer_text")
    private String correctAnswerText; // Lưu đáp án đúng (chuẩn hóa)

    // Cho multiple-choice: ID của đáp án đúng
    @Column(name = "correct_answer_option_id")
    private Long correctAnswerOptionId; // Liên kết với ID của AnswerOption đúng

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC") // Hoặc một trường order khác nếu có
    private Set<AnswerOption> answerOptions; // Danh sách các lựa chọn cho multiple-choice

    public enum QuestionType {
        FILL_IN_THE_BLANK,
        MULTIPLE_CHOICE
    }
}
