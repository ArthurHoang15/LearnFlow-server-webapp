package model.Learning;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description; // Mô tả hoặc tiêu đề của bài học

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LessonFormat format;
    public enum LessonFormat {
        MULTIPLE_CHOICE,
        FILL_IN_THE_BLANK
    }
}


