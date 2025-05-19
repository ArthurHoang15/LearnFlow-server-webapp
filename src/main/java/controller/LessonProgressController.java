package controller;

import dto.LessonProgressDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.LessonProgressService;

@RestController
@RequestMapping("/api/learning/lessons/progress")
public class LessonProgressController {
    @Autowired
    private LessonProgressService lessonProgressService;

    @GetMapping("/{lessonId}")
    public LessonProgressDTO getLessonProgress(@PathVariable Integer lessonId,
                                               @RequestParam Integer userId) {
        return lessonProgressService.getLessonProgress(lessonId, userId);
    }
}
