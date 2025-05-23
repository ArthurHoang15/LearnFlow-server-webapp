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
public class QuestionDto {
    private Long questionId;
    private String type; // "fill-in-the-blank", "multiple-choice"
    private String content;
    private String hint;
    private List<AnswerOptionDto> answers; // Sẽ là null hoặc rỗng cho fill-in-the-blank
}
