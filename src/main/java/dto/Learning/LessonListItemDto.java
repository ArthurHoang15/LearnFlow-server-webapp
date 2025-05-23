package dto.Learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonListItemDto {
    private Long id;
    private String description;
    private String format;      // "multiple-choice" hoặc "fill-in-the-blank"
    private boolean unlocked;   // Luôn là true vì app đơn giản, vô là làm
}
