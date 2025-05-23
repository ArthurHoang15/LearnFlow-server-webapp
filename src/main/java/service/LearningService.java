package service;

// --- DTO Imports ---
import dto.Learning.AnswerAttemptDto;
import dto.Learning.AnswerOptionDto;
import dto.Learning.LessonDetailDto;
import dto.Learning.LessonListItemDto;
import dto.Learning.LessonProgressDto; // Cho ST-61
import dto.Learning.LessonSubmissionResponse;
import dto.Learning.MistakeListItemDto;
import dto.Learning.QuestionDto;
import dto.Learning.ReviewMistakesRequestDto;
import dto.Learning.ReviewSessionDto;
import dto.Learning.SubmitLessonRequest;

// --- Exception Imports ---
import exception.Login.ResourceNotFoundException;

// --- Model Imports (Điều chỉnh package nếu cần) ---
import model.Learning.AnswerOption;
import model.Learning.Lesson;
import model.Learning.LessonProgress;
import model.Learning.Mistake;
import model.Learning.Question;
import model.Learning.Question.QuestionType; // Giả sử đây là Enum: FILL_IN_THE_BLANK, MULTIPLE_CHOICE
import model.User.User;

// --- Repository Imports (Điều chỉnh package nếu cần) ---
import repository.LessonProgressRepository;
import repository.LessonRepository;
import repository.MistakeRepository;
import repository.QuestionRepository;
import repository.UserRepository;
// import repository.AnswerOptionRepository; // Nếu bạn cần truy vấn AnswerOption trực tiếp

// --- Security Imports ---
import security.UserPrincipal;

// --- Lombok and Logging ---
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- Spring Framework Imports ---
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// --- Java Util Imports ---
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningService {

    private static final Logger logger = LoggerFactory.getLogger(LearningService.class);
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final MistakeRepository mistakeRepository;

    /**
     * ST-59: Lấy danh sách tất cả các bài học.
     */
    @Transactional(readOnly = true)
    public Page<LessonListItemDto> getAllLessons(Pageable pageable) {
        logger.info("Fetching all lessons with pagination: {}", pageable);
        Page<Lesson> lessonPage = lessonRepository.findAll(pageable);

        List<LessonListItemDto> dtoList = lessonPage.getContent().stream()
                .map(lesson -> LessonListItemDto.builder()
                        .id(lesson.getId())
                        .description(lesson.getDescription())
                        .format(lesson.getFormat() != null ? (lesson.getFormat() instanceof Enum ? ((Enum<?>) lesson.getFormat()).name() : lesson.getFormat().toString()) : null)
                        .unlocked(true) // Theo yêu cầu đơn giản hóa
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, lessonPage.getTotalElements());
    }

    /**
     * ST-54: Lấy dữ liệu chi tiết của một bài học tương tác.
     */
    @Transactional(readOnly = true)
    public LessonDetailDto getLessonDetails(Long lessonId) {
        logger.info("Fetching details for lesson ID: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", String.valueOf(lessonId)));

        List<Question> questions;
        try {
            // Ưu tiên sử dụng phương thức có sắp xếp nếu repository hỗ trợ
            questions = questionRepository.findByLessonIdOrderByOrderInLessonAsc(lessonId);
            logger.debug("Fetched questions for lesson {} with specific ordering.", lessonId);
        } catch (Exception e) {
            logger.warn("Could not fetch questions with specific ordering for lesson {}. Falling back to default findByLessonId. Error: {}", lessonId, e.getMessage());
            questions = questionRepository.findByLessonId(lessonId);
        }

        List<QuestionDto> questionDtos = questions.stream()
                .map(this::mapQuestionToDto)
                .collect(Collectors.toList());

        return LessonDetailDto.builder()
                .lessonId(lesson.getId())
                .description(lesson.getDescription())
                .questions(questionDtos)
                .build();
    }

    private QuestionDto mapQuestionToDto(Question question) {
        List<AnswerOptionDto> answerOptionDtos = null;
        String questionTypeString = question.getType() instanceof Enum ? ((Enum<?>) question.getType()).name() : question.getType().toString();

        if (QuestionType.MULTIPLE_CHOICE.name().equalsIgnoreCase(questionTypeString) && question.getAnswerOptions() != null) {
            answerOptionDtos = question.getAnswerOptions().stream()
                    .map(option -> AnswerOptionDto.builder()
                            .id(option.getId())
                            .content(option.getContent())
                            .build())
                    .collect(Collectors.toList());
        }

        return QuestionDto.builder()
                .questionId(question.getId())
                .type(questionTypeString)
                .content(question.getContent())
                .hint(question.getHint())
                .answers(answerOptionDtos)
                .build();
    }

    /**
     * ST-60: Xử lý việc người dùng nộp bài cho một bài học.
     */
    @Transactional
    public LessonSubmissionResponse submitLessonAnswers(SubmitLessonRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User {} submitting answers for lesson ID: {}", currentUser.getUsername(), request.getLessonId());

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", String.valueOf(request.getLessonId())));

        List<Question> questionsInDb = questionRepository.findByLessonId(lesson.getId());
        Map<Long, Question> questionMap = questionsInDb.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int correctCountInSubmission = 0;
        int totalQuestionsAttemptedInThisSubmission = request.getAnswers().size();

        for (AnswerAttemptDto userAnswerDto : request.getAnswers()) {
            Question question = questionMap.get(userAnswerDto.getQuestionId());
            if (question == null) {
                logger.warn("User {} submitted answer for non-existent questionId {} in lesson {}",
                        currentUser.getUsername(), userAnswerDto.getQuestionId(), lesson.getId());
                continue;
            }

            boolean isCorrect = false;
            String questionTypeFromDb = question.getType() instanceof Enum ? ((Enum<?>) question.getType()).name() : question.getType().toString();

            if (QuestionType.FILL_IN_THE_BLANK.name().equalsIgnoreCase(questionTypeFromDb)) {
                String correctAnswerText = question.getCorrectAnswerText();
                if (correctAnswerText != null && userAnswerDto.getUserAnswer() != null &&
                        correctAnswerText.trim().equalsIgnoreCase(userAnswerDto.getUserAnswer().trim())) {
                    isCorrect = true;
                }
            } else if (QuestionType.MULTIPLE_CHOICE.name().equalsIgnoreCase(questionTypeFromDb)) {
                Long correctAnswerOptionId = question.getCorrectAnswerOptionId();
                if (correctAnswerOptionId != null &&
                        correctAnswerOptionId.equals(userAnswerDto.getUserAnswerId())) {
                    isCorrect = true;
                }
            } else {
                logger.warn("Unsupported question type '{}' for questionId {}", questionTypeFromDb, question.getId());
            }

            if (isCorrect) {
                correctCountInSubmission++;
            } else {
                Mistake mistake = Mistake.builder() // Sử dụng builder nếu có
                        .user(currentUser)
                        .question(question)
                        .userAnswerText(userAnswerDto.getUserAnswer())
                        .userAnswerOptionId(userAnswerDto.getUserAnswerId())
                        .build();
                // answeredAt sẽ được tự động set bởi @CreationTimestamp
                mistakeRepository.save(mistake);
                logger.debug("Mistake saved for user {} on question {}", currentUser.getUsername(), question.getId());
            }
        }

        int totalQuestionsInLesson = questionsInDb.size();
        double scoreForThisSubmission = (totalQuestionsAttemptedInThisSubmission > 0) ?
                ((double) correctCountInSubmission / totalQuestionsAttemptedInThisSubmission) * 100 : 0;

        LessonProgress lessonProgress = lessonProgressRepository.findByUserIdAndLessonId(currentUser.getId(), lesson.getId())
                .orElseGet(() -> {
                    logger.info("Creating new LessonProgress for user {} and lesson {}", currentUser.getId(), lesson.getId());
                    return LessonProgress.builder() // Sử dụng builder nếu có
                            .user(currentUser)
                            .lesson(lesson)
                            .correctAnswersCount(0)
                            .incorrectAnswersCount(0)
                            .totalQuestionsAttempted(0)
                            .timeSpentSeconds(0L)
                            .score(0.0)
                            .completed(false)
                            .createdAt(LocalDateTime.now()) // Set thời điểm tạo
                            .updatedAt(LocalDateTime.now()) // Set thời điểm cập nhật ban đầu
                            .build();
                });

        // Cập nhật progress
        lessonProgress.setCorrectAnswersCount(lessonProgress.getCorrectAnswersCount() + correctCountInSubmission);
        int incorrectInSubmission = totalQuestionsAttemptedInThisSubmission - correctCountInSubmission;
        lessonProgress.setIncorrectAnswersCount(lessonProgress.getIncorrectAnswersCount() + incorrectInSubmission);
        lessonProgress.setTotalQuestionsAttempted(lessonProgress.getTotalQuestionsAttempted() + totalQuestionsAttemptedInThisSubmission);

        if (lessonProgress.getTotalQuestionsAttempted() > 0) {
            lessonProgress.setScore(((double) lessonProgress.getCorrectAnswersCount() / lessonProgress.getTotalQuestionsAttempted()) * 100);
        } else {
            lessonProgress.setScore(0.0);
        }

        if (request.getTimeSpentSeconds() != null) {
            lessonProgress.setTimeSpentSeconds(lessonProgress.getTimeSpentSeconds() + request.getTimeSpentSeconds());
        } else {
            logger.warn("timeSpentSeconds is null in SubmitLessonRequest for lessonId: {}. No time will be added for this submission.", request.getLessonId());
        }

        // updatedAt sẽ tự động cập nhật bởi @UpdateTimestamp khi save
        // nhưng completedAt cần set thủ công
        if (!lessonProgress.isCompleted() && lessonProgress.getTotalQuestionsAttempted() >= totalQuestionsInLesson) {
            // Logic hoàn thành có thể phức tạp hơn, ví dụ: đạt điểm tối thiểu
            lessonProgress.setCompleted(true);
            lessonProgress.setCompletedAt(LocalDateTime.now());
        }

        LessonProgress savedProgress = lessonProgressRepository.save(lessonProgress); // Lưu lại progress đã cập nhật
        logger.info("Lesson progress updated for user {} on lesson {}: Score {}, Correct {}, Attempted {}, Completed: {}",
                currentUser.getUsername(), lesson.getId(), String.format("%.2f", savedProgress.getScore()),
                savedProgress.getCorrectAnswersCount(), savedProgress.getTotalQuestionsAttempted(), savedProgress.isCompleted());

        // Sử dụng mapLessonProgressToDto để tạo progress DTO đầy đủ cho response
        LessonProgressDto updatedLessonProgressDto = mapLessonProgressToDto(savedProgress);

        return LessonSubmissionResponse.builder()
                .lessonId(lesson.getId())
                .score(scoreForThisSubmission)
                .correctCount(correctCountInSubmission)
                .totalQuestionsAttempted(totalQuestionsAttemptedInThisSubmission)
                .progress(updatedLessonProgressDto) // SỬ DỤNG LessonProgressDto ĐẦY ĐỦ
                .build();
    }

    /**
     * ST-61: Lấy tiến độ của người dùng hiện tại cho một bài học cụ thể.
     */
    @Transactional(readOnly = true)
    public LessonProgressDto getLessonProgress(Long lessonId) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User {} fetching progress for lesson ID: {}", currentUser.getUsername(), lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", String.valueOf(lessonId)));

        LessonProgress lessonProgress = lessonProgressRepository.findByUserIdAndLessonId(currentUser.getId(), lesson.getId())
                .orElseGet(() -> {
                    logger.info("No progress found for user {} and lesson {}. Returning default (not started) progress.", currentUser.getUsername(), lesson.getId());
                    return LessonProgress.builder()
                            .user(currentUser)
                            .lesson(lesson)
                            .correctAnswersCount(0)
                            .incorrectAnswersCount(0)
                            .totalQuestionsAttempted(0)
                            .timeSpentSeconds(0L)
                            .score(0.0)
                            .completed(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                });
        return mapLessonProgressToDto(lessonProgress);
    }

    // Helper method để map LessonProgress entity sang LessonProgressDto
    // ĐẢM BẢO MAP ĐẦY ĐỦ CÁC TRƯỜNG
    private LessonProgressDto mapLessonProgressToDto(LessonProgress lessonProgress) {
        Long lessonProgressLessonId = (lessonProgress.getLesson() != null) ? lessonProgress.getLesson().getId() : null;
        Long lessonProgressUserId = (lessonProgress.getUser() != null) ? lessonProgress.getUser().getId() : null;

        return LessonProgressDto.builder()
                .lessonId(lessonProgressLessonId)
                .userId(lessonProgressUserId)
                .correctCount(lessonProgress.getCorrectAnswersCount())
                .incorrectCount(lessonProgress.getIncorrectAnswersCount())
                .totalQuestionsAttempted(lessonProgress.getTotalQuestionsAttempted())
                .timeSpentSeconds(lessonProgress.getTimeSpentSeconds())
                .score(lessonProgress.getScore())
                .completed(lessonProgress.isCompleted())
                .completedAt(lessonProgress.getCompletedAt())
                .lastAttemptedAt(lessonProgress.getUpdatedAt()) // updatedAt là thời điểm cuối cùng có thay đổi
                .build();
    }


    /**
     * ST-57: Lấy danh sách các lỗi sai của người dùng hiện tại.
     */
    @Transactional(readOnly = true)
    public Page<MistakeListItemDto> getUserMistakes(Long lessonId, Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User {} fetching mistakes. LessonId filter: {}, Pageable: {}", currentUser.getUsername(), lessonId, pageable);

        Page<Mistake> mistakePage = mistakeRepository.findMistakesByUserWithLessonFilter(currentUser, lessonId, pageable);

        List<MistakeListItemDto> dtoList = mistakePage.getContent().stream()
                .map(mistake -> {
                    Question question = mistake.getQuestion();
                    Lesson lesson = question.getLesson();
                    String questionTypeString = question.getType() instanceof Enum ? ((Enum<?>) question.getType()).name() : question.getType().toString();

                    return MistakeListItemDto.builder()
                            .mistakeId(mistake.getId())
                            .questionId(question.getId())
                            .lessonId(lesson != null ? lesson.getId() : null)
                            .questionContentPreview(truncateContent(question.getContent(), 100))
                            .questionType(questionTypeString)
                            .userAnswerText(mistake.getUserAnswerText())
                            .userAnswerOptionId(mistake.getUserAnswerOptionId())
                            .answeredAt(mistake.getAnsweredAt())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, mistakePage.getTotalElements());
    }

    /**
     * ST-57: Lấy thông tin chi tiết các câu hỏi để người dùng ôn tập lỗi sai.
     */
    @Transactional(readOnly = true)
    public ReviewSessionDto getMistakeReviewSession(ReviewMistakesRequestDto request) {
        User currentUser = getCurrentAuthenticatedUser(); // Đảm bảo user đã đăng nhập và có quyền
        logger.info("User {} requesting review session for question IDs: {}", currentUser.getUsername(), request.getQuestionIds());

        if (request.getQuestionIds() == null || request.getQuestionIds().isEmpty()) {
            return ReviewSessionDto.builder().questions(Collections.emptyList()).build();
        }

        List<Question> questionsToReview = questionRepository.findAllById(request.getQuestionIds());
        // TODO (Optional): Thêm logic kiểm tra xem các questionIds này có thực sự là lỗi sai của user không
        // hoặc user có quyền truy cập chúng không, trước khi trả về.

        List<QuestionDto> questionDtos = questionsToReview.stream()
                .map(this::mapQuestionToDto)
                .collect(Collectors.toList());

        return ReviewSessionDto.builder()
                .questions(questionDtos)
                .build();
    }

    // Helper để lấy user hiện tại (quan trọng)
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("User is not authenticated or UserPrincipal is not available. Ensure CustomUserDetailsService returns UserPrincipal.");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", String.valueOf(userPrincipal.getId()) + " (from UserPrincipal)"));
    }

    // Helper để cắt ngắn nội dung (nếu cần)
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
