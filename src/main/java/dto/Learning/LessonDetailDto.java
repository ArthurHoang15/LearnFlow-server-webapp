package dto.Learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDetailDto {
    private Long lessonId;
    private String description;
    private List<QuestionDto> questions;
}
