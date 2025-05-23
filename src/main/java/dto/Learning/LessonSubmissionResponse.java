package dto.Learning; // Hoặc package DTO của bạn

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// import dto.learning.FeedbackDto; // Đã loại bỏ
import dto.Learning.LessonProgressDto; // ĐẢM BẢO IMPORT NÀY (HOẶC XÓA NẾU PROGRESSDTO BỊ XÓA)

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonSubmissionResponse {
    private Long lessonId;
    private double score;
    private int correctCount;
    private int totalQuestionsAttempted;
    // private List<FeedbackDto> feedback; // ĐÃ LOẠI BỎ
    private LessonProgressDto progress; // SỬA KIỂU DỮ LIỆU Ở ĐÂY
}
