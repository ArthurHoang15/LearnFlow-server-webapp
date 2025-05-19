package service;

import dto.LessonProgressDTO;

public interface LessonProgressService {
    LessonProgressDTO getLessonProgress(Integer lessonId, Integer userId);
}
