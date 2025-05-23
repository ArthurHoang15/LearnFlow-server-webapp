package dto.Learning; // Hoặc package DTO tương ứng của bạn

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgressDto {
    private Long lessonId;
    private Long userId;
    private int correctCount;
    private int incorrectCount;
    private int totalQuestionsAttempted;
    private long timeSpentSeconds;
    private double score;
    private boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime lastAttemptedAt; // Sẽ map từ updatedAt của LessonProgress
}
