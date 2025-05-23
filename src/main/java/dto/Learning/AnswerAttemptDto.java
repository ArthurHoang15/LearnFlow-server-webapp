package dto.Learning;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // Thêm Builder nếu bạn muốn sử dụng pattern này khi tạo DTO

@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Constructor không tham số
@AllArgsConstructor // Constructor với tất cả các tham số
@Builder // Cho phép sử dụng builder pattern
public class AnswerAttemptDto {

    @NotNull(message = "Question ID is required")
    private Long questionId; // ID của câu hỏi mà người dùng đang trả lời

    @NotBlank(message = "Question type is required")
    private String type; // Loại câu hỏi, ví dụ: "fill-in-the-blank", "multiple-choice"
    // Điều này giúp backend biết cách xử lý câu trả lời

    // Chỉ một trong hai trường này sẽ có giá trị, tùy thuộc vào 'type' của câu hỏi.
    // Backend sẽ dựa vào 'type' để quyết định đọc trường nào.
    private String userAnswer;      // Dùng cho loại câu hỏi "fill-in-the-blank"
    // Chứa nội dung text mà người dùng điền vào.

    private Long userAnswerId;    // Dùng cho loại câu hỏi "multiple-choice"
    // Chứa ID của lựa chọn (AnswerOption) mà người dùng đã chọn.
}
