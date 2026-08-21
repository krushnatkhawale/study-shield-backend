package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.QuizBundleRequest;
import com.studyshield.studyshield.content.dto.QuizBundleResponse;
import com.studyshield.studyshield.content.dto.QuestionResponse;
import com.studyshield.studyshield.content.dto.QuizResponse;
import com.studyshield.studyshield.content.seed.QuestionBankContent;
import com.studyshield.studyshield.content.entity.*;
import com.studyshield.studyshield.common.exception.InsufficientStockException;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class QuizBundleService {

    public static final int QUIZZES_PER_SUBJECT = QuizBundleSeeder.QUIZZES_PER_SUBJECT;
    /** Minimum real questions a freemium quiz must carry for a session to start. */
    public static final int MIN_ACTIVE_QUESTIONS_PER_QUIZ = 3;

    private final QuizBundleRepository quizBundleRepository;
    private final QuizBundleSeeder catalogSeeder;
    private final SubjectRepository subjectRepository;
    private final ContentPackRepository contentPackRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;

    public QuizBundleService(
            QuizBundleRepository quizBundleRepository,
            QuizBundleSeeder catalogSeeder,
            SubjectRepository subjectRepository,
            ContentPackRepository contentPackRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuestionService questionService
    ) {
        this.quizBundleRepository = quizBundleRepository;
        this.catalogSeeder = catalogSeeder;
        this.subjectRepository = subjectRepository;
        this.contentPackRepository = contentPackRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.questionService = questionService;
    }

    public QuizBundleResponse issue(QuizBundleRequest request) {
        validateHolder(request);
        String className = resolveClassName(request);
        String language = blankToDefault(request.language(), "English");
        String boardCode = blankToDefault(request.boardCode(), "all");
        boolean allowPartial = Boolean.TRUE.equals(request.allowPartial());

        String idempotencyKey = buildKey(className, language, boardCode, request.childId(), request.deviceId());
        return quizBundleRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::toResponse)
                .orElseGet(() -> createBundle(request, className, language, boardCode, idempotencyKey, allowPartial));
    }

    @Transactional(readOnly = true)
    public QuizBundleResponse getById(Long packId) {
        QuizBundle bundle = quizBundleRepository.findById(packId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizBundle", packId));
        return toResponse(bundle);
    }

    private QuizBundleResponse createBundle(
            QuizBundleRequest request,
            String className,
            String language,
            String boardCode,
            String idempotencyKey,
            boolean allowPartial
    ) {
        ClassGrade classGrade = catalogSeeder.ensureCatalogForClass(className, boardCode);
        List<Subject> subjects = subjectRepository.findByClassGradeId(classGrade.getId()).stream()
                .filter(Subject::isActive)
                .toList();
        if (subjects.isEmpty()) {
            throw new ResourceNotFoundException("Subject for class " + className);
        }

        List<Long> quizIds = new ArrayList<>();
        List<String> subjectNames = new ArrayList<>();
        int requiredQuizzes = subjects.size() * QUIZZES_PER_SUBJECT;

        for (Subject subject : subjects) {
            subjectNames.add(subject.getName());
            ContentPack pack = contentPackRepository.findBySubjectId(subject.getId()).stream()
                    .filter(ContentPack::isActive)
                    .filter(p -> p.getName() != null && p.getName().toLowerCase(Locale.ROOT).contains("freemium"))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Freemium ContentPack for subject " + subject.getName()));

            List<Quiz> quizzes = quizRepository
                    .findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                            pack.getId(), ContentTier.FREEMIUM);
            if (quizzes.size() < QUIZZES_PER_SUBJECT && !allowPartial) {
                throw new InsufficientStockException(
                        "Insufficient freemium quizzes for subject " + subject.getName(),
                        quizzes.size(),
                        QUIZZES_PER_SUBJECT
                );
            }
            int take = Math.min(QUIZZES_PER_SUBJECT, quizzes.size());
            for (int i = 0; i < take; i++) {
                Quiz quiz = quizzes.get(i);
                int activeQs = questionRepository.findByQuizIdAndBlacklistedFalse(quiz.getId()).size();
                if (activeQs < MIN_ACTIVE_QUESTIONS_PER_QUIZ && !allowPartial) {
                    throw new InsufficientStockException(
                            "Insufficient questions for quiz " + quiz.getTitle(),
                            activeQs,
                            MIN_ACTIVE_QUESTIONS_PER_QUIZ
                    );
                }
                quizIds.add(quiz.getId());
            }
        }

        if (quizIds.isEmpty()) {
            throw new InsufficientStockException("No freemium quizzes available", 0, requiredQuizzes);
        }

        QuizBundle issued = new QuizBundle();
        issued.setIdempotencyKey(idempotencyKey);
        issued.setClassName(className);
        issued.setLanguage(language);
        issued.setBoardCode(boardCode);
        issued.setDeviceId(request.deviceId());
        issued.setChildId(request.childId());
        issued.setUserId(request.userId());
        issued.setQuizIds(quizIds);
        issued.setSubjects(subjectNames);
        issued.setQuizCount(quizIds.size());
        return toResponse(quizBundleRepository.save(issued));
    }

    private QuizBundleResponse toResponse(QuizBundle bundle) {
        List<QuizResponse> quizzes = new ArrayList<>();
        for (Long quizId : bundle.getQuizIds()) {
            quizRepository.findById(quizId).ifPresent(quiz -> quizzes.add(mapQuizWithQuestions(quiz)));
        }
        return new QuizBundleResponse(
                bundle.getId(),
                bundle.getClassName(),
                bundle.getLanguage(),
                bundle.getBoardCode(),
                bundle.getSubjects(),
                QUIZZES_PER_SUBJECT,
                bundle.getQuizCount(),
                bundle.getDeviceId(),
                bundle.getChildId(),
                bundle.getUserId(),
                quizzes,
                bundle.getCreatedAt()
        );
    }

    private QuizResponse mapQuizWithQuestions(Quiz quiz) {
        List<QuestionResponse> questions = questionRepository
                .findByQuizIdAndBlacklistedFalse(quiz.getId()).stream()
                .map(questionService::mapToResponse)
                .toList();
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getContentPack().getId(),
                quiz.getContentPack().getName(),
                quiz.getQuizType(),
                quiz.getQuestionCount(),
                quiz.getContentTier(),
                quiz.getFreemiumIndex(),
                quiz.getLanguage(),
                quiz.isActive(),
                questions,
                quiz.getCreatedAt(),
                quiz.getUpdatedAt()
        );
    }

    private void validateHolder(QuizBundleRequest request) {
        if (request.childId() == null && (request.deviceId() == null || request.deviceId().isBlank())) {
            throw new IllegalArgumentException("Either childId or deviceId is required");
        }
        if ((request.className() == null || request.className().isBlank()) && request.age() == null) {
            throw new IllegalArgumentException("Either className or age is required");
        }
    }

    /**
     * Class drives question filtering. When className is absent, derive it from the child's
     * age so sessions are still age-appropriate (issue #1: filter by class/age).
     */
    private static String resolveClassName(QuizBundleRequest request) {
        String className = request.className();
        if (className != null && !className.isBlank()) {
            return QuizBundleSeeder.normalizeClassName(className);
        }
        return QuestionBankContent.classNameForAge(request.age());
    }

    private static String buildKey(String className, String language, String boardCode, Long childId, String deviceId) {
        String holder = childId != null ? "child:" + childId : "device:" + deviceId.trim();
        return className.trim().toLowerCase(Locale.ROOT) + "|"
                + language.trim().toLowerCase(Locale.ROOT) + "|"
                + boardCode.trim().toLowerCase(Locale.ROOT) + "|"
                + holder;
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
