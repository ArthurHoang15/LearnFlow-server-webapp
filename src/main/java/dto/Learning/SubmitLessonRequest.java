package dto.Learning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitLessonRequest {

    @NotNull(message = "Lesson ID is required")
    private Long lessonId;

    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<AnswerAttemptDto> answers;

    @NotNull(message = "Time spent is required")
    @Min(value = 0, message = "Time spent must be non-negative") // Thời gian không thể âm
    private Long timeSpentSeconds; // BỎ COMMENT VÀ THÊM VALIDATION
}
