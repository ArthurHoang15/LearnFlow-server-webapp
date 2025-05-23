package model.Learning; // Hoặc model.Learning nếu bạn nhóm theo module

import jakarta.persistence.*;
import lombok.*; // Thêm @Builder, @AllArgsConstructor nếu cần
import model.User.User;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;

@Entity
@Table(name = "mistakes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Thêm nếu muốn constructor với tất cả args
@Builder        // Thêm nếu muốn dùng builder pattern
public class Mistake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional = false nghĩa là mistake luôn phải có user
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional = false nghĩa là mistake luôn phải có question
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_answer_text", length = 1000)
    private String userAnswerText; // Cho fill-in-the-blank

    @Column(name = "user_answer_option_id")
    private Long userAnswerOptionId; // Cho multiple-choice

    @CreationTimestamp
    @Column(name = "answered_at", updatable = false, nullable = false)
    private LocalDateTime answeredAt;
}
