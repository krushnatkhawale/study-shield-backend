package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.FreemiumRebuildResponse;
import com.studyshield.studyshield.content.dto.QuizBundleRequest;
import com.studyshield.studyshield.content.dto.QuizBundleResponse;
import com.studyshield.studyshield.content.dto.QuestionResponse;
import com.studyshield.studyshield.content.dto.QuizResponse;
import com.studyshield.studyshield.content.seed.QuestionBankContent;
import com.studyshield.studyshield.content.entity.*;
import com.studyshield.studyshield.common.exception.InsufficientStockException;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class QuizBundleService {

    /** One freemium quiz is issued per subject that has questions. */
    public static final int QUIZZES_PER_SUBJECT = 1;
    /** Minimum real questions a freemium quiz must carry for a session to start. */
    public static final int MIN_ACTIVE_QUESTIONS_PER_QUIZ = 3;

    private final QuizBundleRepository quizBundleRepository;
    private final QuizBundleSeeder catalogSeeder;
    private final ClassGradeRepository classGradeRepository;
    private final SubjectRepository subjectRepository;
    private final ContentPackRepository contentPackRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;

    public QuizBundleService(
            QuizBundleRepository quizBundleRepository,
            QuizBundleSeeder catalogSeeder,
            ClassGradeRepository classGradeRepository,
            SubjectRepository subjectRepository,
            ContentPackRepository contentPackRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuestionService questionService
    ) {
        this.quizBundleRepository = quizBundleRepository;
        this.catalogSeeder = catalogSeeder;
        this.classGradeRepository = classGradeRepository;
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
    public List<QuizBundleResponse> getAll() {
        return quizBundleRepository.findAll().stream().map(this::toResponse).toList();
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
        List<Subject> subjects = subjectRepository.findByClassGradeIdOrderByDisplayOrderAscIdAsc(classGrade.getId()).stream()
                .filter(Subject::isActive)
                .toList();
        if (subjects.isEmpty()) {
            throw new InsufficientStockException("No subjects for class " + className, 0, 1);
        }

        List<Long> quizIds = new ArrayList<>();
        List<String> subjectNames = new ArrayList<>();

        for (Subject subject : subjects) {
            ContentPack pack = QuizBundleSeeder.pickActiveDeliveryPack(
                    contentPackRepository.findBySubjectId(subject.getId()));
            if (pack == null) {
                continue;
            }

            List<Quiz> quizzes = quizRepository
                    .findByContentPackIdAndContentTierAndActiveTrueOrderByFreemiumIndexAsc(
                            pack.getId(), ContentTier.FREEMIUM);
            Quiz chosen = null;
            for (Quiz quiz : quizzes) {
                int activeQs = questionRepository.findByQuizIdAndBlacklistedFalse(quiz.getId()).size();
                if (activeQs >= MIN_ACTIVE_QUESTIONS_PER_QUIZ
                        || (allowPartial && activeQs > 0)) {
                    chosen = quiz;
                    break;
                }
            }
            if (chosen == null) {
                continue;
            }
            subjectNames.add(subject.getName());
            quizIds.add(chosen.getId());
        }

        if (quizIds.isEmpty()) {
            throw new InsufficientStockException("No freemium quizzes available", 0, 1);
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
        try {
            return toResponse(quizBundleRepository.saveAndFlush(issued));
        } catch (DataIntegrityViolationException e) {
            return quizBundleRepository.findByIdempotencyKey(idempotencyKey)
                    .map(this::toResponse)
                    .orElseThrow(() -> e);
        }
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

    /**
     * Drop issued (idempotent) bundles so kids get a fresh pack list, then seed
     * one freemium quiz for every class/subject that has bank or loaded questions.
     * Not one giant transaction — each class seed runs in {@code ensureCatalogForClass}'s TX.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FreemiumRebuildResponse rebuildFreemiumCatalog() {
        long deletedBundles = quizBundleRepository.count();
        quizBundleRepository.deleteAllInBatch();

        LinkedHashSet<String> classes = new LinkedHashSet<>(QuestionBankContent.BANK.keySet());
        for (ClassGrade grade : classGradeRepository.findAll()) {
            if (grade.getName() != null && !grade.getName().isBlank()) {
                classes.add(grade.getName());
            }
        }

        List<String> seeded = new ArrayList<>();
        for (String className : classes) {
            catalogSeeder.ensureCatalogForClass(className, "ALL");
            seeded.add(className);
        }
        return new FreemiumRebuildResponse(deletedBundles, seeded.size(), seeded);
    }
}
