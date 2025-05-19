package service;

import dto.Learning.*; // Import tất cả DTOs từ learning
import exception.Login.ResourceNotFoundException;
import model.Learning.Lesson;
import model.Learning.Question;
import model.User.User;
import repository.*; // Import UserRepository, LessonRepository, QuestionRepository, LessonProgressRepository, MistakeRepository
import repository.UserRepository;
import repository.LessonRepository;
import repository.QuestionRepository;
import security.UserPrincipal; // Import UserPrincipal

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication; // Thêm import
import org.springframework.security.core.context.SecurityContextHolder; // Thêm import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime; // Thêm import
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningService {

    private static final Logger logger = LoggerFactory.getLogger(LearningService.class);
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository; // Cần để lấy User entity
    private final LessonProgressRepository lessonProgressRepository; // Cần cho ST-60
    private final MistakeRepository mistakeRepository;             // Cần cho ST-60
    // private final AnswerOptionRepository answerOptionRepository; // Nếu cần

    @Transactional(readOnly = true)
    public Page<LessonListItemDto> getAllLessons(Pageable pageable) {
        logger.info("Fetching all lessons with pagination: {}", pageable);
        Page<Lesson> lessonPage = lessonRepository.findAll(pageable);

        List<LessonListItemDto> dtoList = lessonPage.getContent().stream()
                .map(lesson -> LessonListItemDto.builder()
                        .id(lesson.getId())
                        .description(lesson.getDescription())
                        .format(lesson.getFormat() != null ? lesson.getFormat().name() : null) // SỬ DỤNG .name() cho enum
                        .unlocked(true)
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, lessonPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public LessonDetailDto getLessonDetails(Long lessonId) {
        logger.info("Fetching details for lesson ID: {}", lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", String.valueOf(lessonId)));

        // Đảm bảo QuestionRepository có phương thức này hoặc dùng findByLessonId
        List<Question> questions = questionRepository.findByLessonIdOrderByOrderInLessonAsc(lessonId);
        // List<Question> questions = questionRepository.findByLessonId(lessonId); // Nếu không có sắp xếp


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
        if ("multiple-choice".equalsIgnoreCase(question.getType()) && question.getAnswerOptions() != null) {
            answerOptionDtos = question.getAnswerOptions().stream()
                    .map(option -> AnswerOptionDto.builder()
                            .id(option.getId())
                            .content(option.getContent())
                            .build())
                    .collect(Collectors.toList());
        }

        return QuestionDto.builder()
                .questionId(question.getId())
                .type(question.getType())
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
        // 1. Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("User is not authenticated or UserPrincipal is not available.");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", String.valueOf(userPrincipal.getId())));
        logger.info("User {} submitting answers for lesson ID: {}", currentUser.getUsername(), request.getLessonId());

        // 2. Lấy thông tin bài học
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", String.valueOf(request.getLessonId())));

        // 3. Lấy tất cả câu hỏi của bài học từ DB để đối chiếu và lấy đáp án đúng
        List<Question> questionsInDb = questionRepository.findByLessonId(lesson.getId()); // Hoặc có sắp xếp nếu cần
        Map<Long, Question> questionMap = questionsInDb.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int correctCountInSubmission = 0;
        int totalQuestionsAttemptedInThisSubmission = request.getAnswers().size();
        List<FeedbackDto> feedbackList = new ArrayList<>();

        // 4. Xử lý từng câu trả lời
        for (AnswerAttemptDto userAnswerDto : request.getAnswers()) {
            Question question = questionMap.get(userAnswerDto.getQuestionId());
            if (question == null) {
                logger.warn("User {} submitted answer for non-existent questionId {} in lesson {}",
                        currentUser.getUsername(), userAnswerDto.getQuestionId(), lesson.getId());
                feedbackList.add(FeedbackDto.builder()
                        .questionId(userAnswerDto.getQuestionId())
                        .isCorrect(false)
                        .userAnswer(userAnswerDto.getUserAnswer())
                        .userAnswerId(userAnswerDto.getUserAnswerId())
                        .build());
                continue;
            }

            boolean isCorrect = false;
            FeedbackDto.FeedbackDtoBuilder feedbackBuilder = FeedbackDto.builder()
                    .questionId(question.getId())
                    .userAnswer(userAnswerDto.getUserAnswer())
                    .userAnswerId(userAnswerDto.getUserAnswerId());

            if ("fill-in-the-blank".equalsIgnoreCase(question.getType())) {
                String correctAnswerText = question.getCorrectAnswerText();
                feedbackBuilder.correctAnswer(correctAnswerText);
                if (correctAnswerText != null && userAnswerDto.getUserAnswer() != null &&
                        correctAnswerText.trim().equalsIgnoreCase(userAnswerDto.getUserAnswer().trim())) {
                    isCorrect = true;
                }
            } else if ("multiple-choice".equalsIgnoreCase(question.getType())) {
                Long correctAnswerOptionId = question.getCorrectAnswerOptionId();
                feedbackBuilder.correctAnswerId(correctAnswerOptionId);
                if (correctAnswerOptionId != null &&
                        correctAnswerOptionId.equals(userAnswerDto.getUserAnswerId())) {
                    isCorrect = true;
                }
            } else {
                logger.warn("Unsupported question type '{}' for questionId {}", question.getType(), question.getId());
            }

            feedbackBuilder.isCorrect(isCorrect);
            feedbackList.add(feedbackBuilder.build());

            if (isCorrect) {
                correctCountInSubmission++;
            } else {
                Mistake mistake = new Mistake();
                mistake.setUser(currentUser);
                mistake.setQuestion(question);
                mistake.setUserAnswerText(userAnswerDto.getUserAnswer());
                mistake.setUserAnswerOptionId(userAnswerDto.getUserAnswerId());
                mistakeRepository.save(mistake);
                logger.debug("Mistake saved for user {} on question {}", currentUser.getUsername(), question.getId());
            }
        }

        int totalQuestionsInLesson = questionsInDb.size();
        double scoreForThisSubmission = (totalQuestionsAttemptedInThisSubmission > 0) ?
                ((double) correctCountInSubmission / totalQuestionsAttemptedInThisSubmission) * 100 : 0;

        // 6. Cập nhật bảng lesson_progress
        LessonProgress lessonProgress = lessonProgressRepository.findByUserIdAndLessonId(currentUser.getId(), lesson.getId())
                .orElseGet(() -> {
                    logger.info("Creating new LessonProgress for user {} and lesson {}", currentUser.getId(), lesson.getId());
                    LessonProgress newProgress = new LessonProgress();
                    newProgress.setUser(currentUser);
                    newProgress.setLesson(lesson);
                    return newProgress;
                });

        // Cập nhật progress. Ví dụ:
        lessonProgress.setCorrectAnswersCount(lessonProgress.getCorrectAnswersCount() + correctCountInSubmission);
        lessonProgress.setIncorrectAnswersCount(lessonProgress.getIncorrectAnswersCount() + (totalQuestionsAttemptedInThisSubmission - correctCountInSubmission));
        lessonProgress.setTotalQuestionsAttempted(lessonProgress.getTotalQuestionsAttempted() + totalQuestionsAttemptedInThisSubmission);

        // Cập nhật điểm số (ví dụ: lấy điểm trung bình hoặc điểm cao nhất)
        // Hiện tại, tính lại score dựa trên tổng số câu đúng và tổng số câu đã thử tích lũy
        if (lessonProgress.getTotalQuestionsAttempted() > 0) {
            lessonProgress.setScore(((double) lessonProgress.getCorrectAnswersCount() / lessonProgress.getTotalQuestionsAttempted()) * 100);
        } else {
            lessonProgress.setScore(0.0);
        }


        // Sử dụng timeSpentSeconds từ request
        if (request.getTimeSpentSeconds() != null) {
            lessonProgress.setTimeSpentSeconds(lessonProgress.getTimeSpentSeconds() + request.getTimeSpentSeconds());
        } else {
            // Xử lý nếu client không gửi (ví dụ, log cảnh báo hoặc dùng giá trị mặc định)
            logger.warn("timeSpentSeconds is null in SubmitLessonRequest for lessonId: {}", request.getLessonId());
             lessonProgress.setTimeSpentSeconds(lessonProgress.getTimeSpentSeconds() + 1L);
        }


            // Kiểm tra hoàn thành
        // Ví dụ: hoàn thành nếu đã thử tất cả các câu của bài học
        if (lessonProgress.getTotalQuestionsAttempted() >= totalQuestionsInLesson) {
            lessonProgress.setCompleted(true);
            if (lessonProgress.getCompletedAt() == null) { // Chỉ set lần đầu hoàn thành
                lessonProgress.setCompletedAt(LocalDateTime.now());
            }
        }

        lessonProgressRepository.save(lessonProgress);
        logger.info("Lesson progress updated for user {} on lesson {}: Score {}, Completed {}",
                currentUser.getUsername(), lesson.getId(), lessonProgress.getScore(), lessonProgress.isCompleted());

        // 7. Chuẩn bị response
        ProgressDto progressDto = ProgressDto.builder()
                .correctCount(lessonProgress.getCorrectAnswersCount())
                .incorrectCount(lessonProgress.getIncorrectAnswersCount())
                .timeSpent(lessonProgress.getTimeSpentSeconds())
                .completed(lessonProgress.isCompleted())
                .build();

        return LessonSubmissionResponse.builder()
                .lessonId(lesson.getId())
                .score(scoreForThisSubmission) // Điểm của lần submit này
                .correctCount(correctCountInSubmission)
                .totalQuestionsAttempted(totalQuestionsAttemptedInThisSubmission)
                .feedback(feedbackList)
                .progress(progressDto) // Progress tổng thể của bài học
                .build();
    }
}
