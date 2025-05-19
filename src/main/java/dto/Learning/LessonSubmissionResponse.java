package dto.Learning;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonSubmissionResponse {
    private Long lessonId;
    private double score; // Tính theo % hoặc điểm (0.0 - 100.0)
    private int correctCount; // Số câu đúng trong lần submit này
    private int totalQuestionsAttempted; // Số câu đã thử trong lần submit này
    // private int totalQuestionsInLesson; // Tùy chọn: Tổng số câu trong bài học
    private List<FeedbackDto> feedback; // Feedback cho từng câu đã trả lời
    private ProgressDto progress; // Thông tin tiến độ tổng thể của bài học sau khi submit
}
