package dto.Learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MistakeListItemDto {
    private Long mistakeId;
    private Long questionId;
    private Long lessonId; // Quan trọng: để biết lỗi này thuộc bài học nào
    private String questionContentPreview;
    private String questionType;
    private String userAnswerText;
    private Long userAnswerOptionId;
    private LocalDateTime answeredAt;
}
