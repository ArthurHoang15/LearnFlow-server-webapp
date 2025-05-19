package dto.Learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// import dto.learning.QuestionDto; // Đảm bảo import này nếu QuestionDto ở cùng package

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSessionDto {
    private List<QuestionDto> questions; // Sử dụng lại QuestionDto từ ST-54
}
