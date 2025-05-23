package service;


import dto.request.LessonCreateRequest;
import dto.request.LessonUpdateRequest;
import entity.Lesson;

public interface LessonService {
    Lesson createLesson(LessonCreateRequest request);
    Lesson updateLesson(Integer lessonId, LessonUpdateRequest request);
    void deleteLesson(Integer lessonId);
}
