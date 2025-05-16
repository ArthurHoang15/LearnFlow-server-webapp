package service.impl;

import dto.request.LessonCreateRequest;
import dto.request.LessonUpdateRequest;
import entity.Lesson;
import exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.LessonRepository;
import service.LessonService;

@Service
public class LessonServiceImpl implements LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Override
    public Lesson createLesson(LessonCreateRequest request) {
        Lesson lesson = new Lesson();
        lesson.setChapterId(request.getChapterId());
        lesson.setDescription(request.getDescription());
        return lessonRepository.save(lesson);
    }

    @Override
    public Lesson updateLesson(Integer lessonId, LessonUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        lesson.setChapterId(request.getChapterId());
        lesson.setDescription(request.getDescription());
        return lessonRepository.save(lesson);
    }

    @Override
    public void deleteLesson(Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + lessonId));
        lessonRepository.delete(lesson);
    }
}
