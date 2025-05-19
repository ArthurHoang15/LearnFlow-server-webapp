package service.impl;

import dto.LessonProgressDTO;
import entity.LessonProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.LessonProgressRepository;
import service.LessonProgressService;

@Service
public class LessonProgressServiceImpl implements LessonProgressService {
    @Autowired
    private LessonProgressRepository lessonProgressRepository;
    @Override
    public LessonProgressDTO getLessonProgress(Integer lessonId, Integer userId) {
        LessonProgress progress = lessonProgressRepository.findByLessonIdAndUserId(lessonId, userId)
                .orElseThrow(() -> new RuntimeException("Progress not found for lessonId: " + lessonId));

        LessonProgressDTO dto = new LessonProgressDTO();
        dto.setLessonId(progress.getLessonId());
        dto.setCorrectCount(progress.getCorrectCount());
        dto.setIncorrectCount(progress.getIncorrectCount());
        dto.setTimeSpent(progress.getTimeSpent());
        // Giả sử completed dựa trên điều kiện, ví dụ: correct_count >= 4
        dto.setCompleted(progress.getCorrectCount() >= 4);

        return dto;
    }

}
