package dto.Learning;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull; // Thêm nếu cần cho từng ID
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMistakesRequestDto {
    @NotEmpty(message = "List of question IDs cannot be empty")
    // @NotNull // Có thể không cần nếu @NotEmpty đã bao gồm, tùy thuộc vào phiên bản validation
    private List<Long> questionIds;
}
