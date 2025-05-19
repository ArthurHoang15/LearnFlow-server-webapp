package service;

// THAY THẾ WILDCARD IMPORTS BẰNG IMPORT CỤ THỂ
import dto.Learning.QuestionDto;
import dto.Learning.AnswerAttemptDto;
import dto.Learning.AnswerOptionDto;
import dto.Learning.LessonDetailDto;
import dto.Learning.LessonListItemDto;
import dto.Learning.LessonSubmissionResponse;
import dto.Learning.ProgressDto;
import dto.Learning.ReviewMistakesRequestDto;
import dto.Learning.ReviewSessionDto;
import dto.Learning.SubmitLessonRequest;
import dto.Learning.MistakeListItemDto; // Thêm nếu chưa có

import exception.Login.ResourceNotFoundException;

import model.Learning.Lesson; // Điều chỉnh package nếu Lesson ở model.Learning
import model.Learning.Question; // Điều chỉnh package nếu Question ở model.Learning
import model.Learning.Question.QuestionType; // Điều chỉnh package nếu QuestionType ở model.Learning
import model.User.User; // Điều chỉnh package nếu User ở model.User
import model.LessonProgress;
import model.Learning.Mistake;
import model.Learning.AnswerOption; // Nếu Question.getAnswerOptions() trả về Set<AnswerOption>

import repository.LessonRepository;
import repository.QuestionRepository;
import repository.UserRepository;
import repository.LessonProgressRepository;
import repository.MistakeRepository;
// import repository.AnswerOptionRepository; // Nếu cần

import security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList; // Vẫn cần cho list khác nếu có
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

    @Transactional(readOnly = true)
    public Page<LessonListItemDto> getAllLessons(Pageable pageable) {
        logger.info("Fetching all lessons with pagination: {}", pageable);
        Page<Lesson> lessonPage = lessonRepository.findAll(pageable);

        List<LessonListItemDto> dtoList = lessonPage.getContent().stream()
                .map(lesson -> LessonListItemDto.builder()
                        .id(lesson.getId())
                        .description(lesson.getDescription())
                        .format(lesson.getFormat() != null ? (lesson.getFormat() instanceof Enum ? ((Enum<?>) lesson.getFormat()).name() : lesson.getFormat().toString()) : null)
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

        List<Question> questions;
        // Kiểm tra xem QuestionRepository có phương thức sắp xếp không
        // if (phương thức findByLessonIdOrderByOrderInLessonAsc tồn tại) {
        //     questions = questionRepository.findByLessonIdOrderByOrderInLessonAsc(lessonId);
        // } else {
        questions = questionRepository.findByLessonId(lessonId);
        // }
        // Giả sử bạn đã có findByLessonIdOrderByOrderInLessonAsc hoặc bạn sẽ thêm nó
        // questions = questionRepository.findByLessonIdOrderByOrderInLessonAsc(lessonId);


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
        // List<FeedbackDto> feedbackList = new ArrayList<>(); // LOẠI BỎ feedbackList

        for (AnswerAttemptDto userAnswerDto : request.getAnswers()) {
            Question question = questionMap.get(userAnswerDto.getQuestionId());
            if (question == null) {
                logger.warn("User {} submitted answer for non-existent questionId {} in lesson {}",
                        currentUser.getUsername(), userAnswerDto.getQuestionId(), lesson.getId());
                // Không tạo feedback nữa
                continue;
            }

            boolean isCorrect = false;
            // Không cần FeedbackDto.FeedbackDtoBuilder nữa

            String questionTypeFromDb = question.getType() instanceof Enum ? ((Enum<?>) question.getType()).name() : question.getType().toString();

            if (QuestionType.FILL_IN_THE_BLANK.name().equalsIgnoreCase(questionTypeFromDb)) {
                String correctAnswerText = question.getCorrectAnswerText();
                // feedbackBuilder.correctAnswer(correctAnswerText); // LOẠI BỎ
                if (correctAnswerText != null && userAnswerDto.getUserAnswer() != null &&
                        correctAnswerText.trim().equalsIgnoreCase(userAnswerDto.getUserAnswer().trim())) {
                    isCorrect = true;
                }
            } else if (QuestionType.MULTIPLE_CHOICE.name().equalsIgnoreCase(questionTypeFromDb)) {
                Long correctAnswerOptionId = question.getCorrectAnswerOptionId();
                // feedbackBuilder.correctAnswerId(correctAnswerOptionId); // LOẠI BỎ
                if (correctAnswerOptionId != null &&
                        correctAnswerOptionId.equals(userAnswerDto.getUserAnswerId())) {
                    isCorrect = true;
                }
            } else {
                logger.warn("Unsupported question type '{}' for questionId {}", questionTypeFromDb, question.getId());
            }

            // feedbackBuilder.isCorrect(isCorrect); // LOẠI BỎ
            // feedbackList.add(feedbackBuilder.build()); // LOẠI BỎ

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

        LessonProgress lessonProgress = lessonProgressRepository.findByUserIdAndLessonId(currentUser.getId(), lesson.getId())
                .orElseGet(() -> {
                    logger.info("Creating new LessonProgress for user {} and lesson {}", currentUser.getId(), lesson.getId());
                    LessonProgress newProgress = new LessonProgress();
                    newProgress.setUser(currentUser);
                    newProgress.setLesson(lesson);
                    return newProgress;
                });

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
            logger.warn("timeSpentSeconds is null in SubmitLessonRequest for lessonId: {}. Adding default time.", request.getLessonId());
            lessonProgress.setTimeSpentSeconds(lessonProgress.getTimeSpentSeconds() + 60L);
        }

        if (lessonProgress.getTotalQuestionsAttempted() >= totalQuestionsInLesson) {
            lessonProgress.setCompleted(true);
            if (lessonProgress.getCompletedAt() == null) {
                lessonProgress.setCompletedAt(LocalDateTime.now());
            }
        }

        lessonProgressRepository.save(lessonProgress);
        logger.info("Lesson progress updated for user {} on lesson {}: Score {}, Completed: {}",
                currentUser.getUsername(), lesson.getId(), String.format("%.2f", lessonProgress.getScore()), lessonProgress.isCompleted());

        ProgressDto progressDto = ProgressDto.builder()
                .correctCount(lessonProgress.getCorrectAnswersCount())
                .incorrectCount(lessonProgress.getIncorrectAnswersCount())
                .timeSpent(lessonProgress.getTimeSpentSeconds())
                .completed(lessonProgress.isCompleted())
                .build();

        return LessonSubmissionResponse.builder()
                .lessonId(lesson.getId())
                .score(scoreForThisSubmission)
                .correctCount(correctCountInSubmission)
                .totalQuestionsAttempted(totalQuestionsAttemptedInThisSubmission)
                // .feedback(feedbackList) // LOẠI BỎ feedbackList khỏi response
                .progress(progressDto)
                .build();
    }

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

    @Transactional(readOnly = true)
    public ReviewSessionDto getMistakeReviewSession(ReviewMistakesRequestDto request) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User {} requesting review session for question IDs: {}", currentUser.getUsername(), request.getQuestionIds());

        if (request.getQuestionIds() == null || request.getQuestionIds().isEmpty()) {
            return ReviewSessionDto.builder().questions(Collections.emptyList()).build();
        }

        List<Question> questionsToReview = questionRepository.findAllById(request.getQuestionIds());

        List<QuestionDto> questionDtos = questionsToReview.stream()
                .map(this::mapQuestionToDto)
                .collect(Collectors.toList());

        return ReviewSessionDto.builder()
                .questions(questionDtos)
                .build();
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("User is not authenticated or UserPrincipal is not available. Ensure CustomUserDetailsService returns UserPrincipal.");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", String.valueOf(userPrincipal.getId()) + " (from UserPrincipal)"));
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
