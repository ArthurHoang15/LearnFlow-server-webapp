package controller;

import dto.request.LessonUpdateRequest;
import dto.response.ApiResponse;
import dto.request.LessonCreateRequest;
import entity.Lesson;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.LessonService;

@RestController
@RequestMapping("/api/admin/lessons")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @PostMapping
    public ResponseEntity<ApiResponse<Lesson>> createLesson(@Valid @RequestBody LessonCreateRequest request) {
        try {
            Lesson lesson = lessonService.createLesson(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Thêm bài học thành công", lesson));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi thêm bài học: " + e.getMessage()));
        }
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<Lesson>> updateLesson(
            @PathVariable Integer lessonId,
            @Valid @RequestBody LessonUpdateRequest request) {
        try {
            Lesson lesson = lessonService.updateLesson(lessonId, request);
            return ResponseEntity.ok()
                    .body(ApiResponse.success("Cập nhật bài học thành công", lesson));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi cập nhật bài học: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Integer lessonId) {
        try {
            lessonService.deleteLesson(lessonId);
            return ResponseEntity.ok()
                    .body(ApiResponse.success("Xóa bài học thành công", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi xóa bài học: " + e.getMessage()));
        }
    }
}
