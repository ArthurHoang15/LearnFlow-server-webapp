package controller;

import dto.Learning.LessonListItemDto;
import dto.Learning.LessonDetailDto;
import dto.Learning.LessonSubmissionResponse;
import dto.Learning.MistakeListItemDto;
import dto.Learning.ReviewMistakesRequestDto;
import dto.Learning.ReviewSessionDto;
import dto.Learning.LessonProgressDto;
import dto.Learning.SubmitLessonRequest;

import service.LearningService;

import jakarta.validation.Valid; // THÊM IMPORT
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*; // Import @Valid

@RestController
@RequestMapping("/api/learning/lessons")
@RequiredArgsConstructor
public class LearningController {

    private static final Logger logger = LoggerFactory.getLogger(LearningController.class);
    private final LearningService learningService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<LessonListItemDto>> getAllLessons(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        logger.info("Received request to get all lessons with pageable: {}", pageable);
        Page<LessonListItemDto> lessons = learningService.getAllLessons(pageable);
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonDetailDto> getLessonDetails(@PathVariable Long lessonId) {
        logger.info("Received request to get details for lesson ID: {}", lessonId);
        LessonDetailDto lessonDetail = learningService.getLessonDetails(lessonId);
        return ResponseEntity.ok(lessonDetail);
    }

    /**
     * ST-60: API gửi câu trả lời của người dùng cho một bài học.
     */
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonSubmissionResponse> submitLessonAnswers(
            @Valid @RequestBody SubmitLessonRequest request) { // THÊM @Valid
        logger.info("Received request from user to submit answers for lesson ID: {}", request.getLessonId());
        LessonSubmissionResponse response = learningService.submitLessonAnswers(request);
        logger.info("Successfully processed answer submission for lesson ID: {}. Score for this submission: {}", request.getLessonId(), response.getScore());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mistakes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MistakeListItemDto>> getUserMistakes(
            @RequestParam(required = false) Long lessonId,
            @PageableDefault(size = 10, sort = "answeredAt,desc") Pageable pageable) {
        logger.info("Received request to get user mistakes. LessonId filter: {}, Pageable: {}", lessonId, pageable);
        Page<MistakeListItemDto> mistakes = learningService.getUserMistakes(lessonId, pageable);
        return ResponseEntity.ok(mistakes);
    }

    @PostMapping("/mistakes/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewSessionDto> reviewMistakes(
            @Valid @RequestBody ReviewMistakesRequestDto request) {
        logger.info("Received request to review mistakes for question IDs: {}", request.getQuestionIds());
        ReviewSessionDto reviewSession = learningService.getMistakeReviewSession(request);
        return ResponseEntity.ok(reviewSession);
    }

    /**
     * ST-61: API lấy tiến độ của người dùng hiện tại cho một bài học cụ thể.
     */
    @GetMapping("/progress/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonProgressDto> getLessonProgress(@PathVariable Long lessonId) {
        logger.info("Received request to get progress for lesson ID: {}", lessonId);
        LessonProgressDto progressDto = learningService.getLessonProgress(lessonId);
        // Không cần kiểm tra null ở đây nữa nếu orElseGet trong service luôn tạo object
        return ResponseEntity.ok(progressDto);
    }
}
