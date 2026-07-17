package com.studyshield.content.service;

import com.studyshield.content.dto.FreemiumPackRequest;
import com.studyshield.content.dto.FreemiumPackResponse;
import com.studyshield.content.dto.QuestionResponse;
import com.studyshield.content.dto.QuizResponse;
import com.studyshield.content.entity.*;
import com.studyshield.content.exception.InsufficientStockException;
import com.studyshield.content.exception.ResourceNotFoundException;
import com.studyshield.content.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class FreemiumPackService {

    public static final int FREEMIUM_QUIZZES_PER_SUBJECT = FreemiumCatalogSeeder.QUIZZES_PER_SUBJECT;

    private final FreemiumPackRepository freemiumPackRepository;
    private final FreemiumCatalogSeeder catalogSeeder;
    private final SubjectRepository subjectRepository;
    private final ContentPackRepository contentPackRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;

    public FreemiumPackService(
            FreemiumPackRepository freemiumPackRepository,
            FreemiumCatalogSeeder catalogSeeder,
            SubjectRepository subjectRepository,
            ContentPackRepository contentPackRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuestionService questionService
    ) {
        this.freemiumPackRepository = freemiumPackRepository;
        this.catalogSeeder = catalogSeeder;
        this.subjectRepository = subjectRepository;
        this.contentPackRepository = contentPackRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.questionService = questionService;
    }

    public FreemiumPackResponse issue(FreemiumPackRequest request) {
        validateHolder(request);
        String className = FreemiumCatalogSeeder.normalizeClassName(request.className());
        String language = blankToDefault(request.language(), "English");
        String boardCode = blankToDefault(request.boardCode(), "all");
        boolean allowPartial = Boolean.TRUE.equals(request.allowPartial());

        String idempotencyKey = buildKey(className, language, boardCode, request.childId(), request.deviceId());
        return freemiumPackRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::toResponse)
                .orElseGet(() -> createPack(request, className, language, boardCode, idempotencyKey, allowPartial));
    }

    @Transactional(readOnly = true)
    public FreemiumPackResponse getById(Long packId) {
        FreemiumPack pack = freemiumPackRepository.findById(packId)
                .orElseThrow(() -> new ResourceNotFoundException("FreemiumPack", packId));
        return toResponse(pack);
    }

    private FreemiumPackResponse createPack(
            FreemiumPackRequest request,
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
        int requiredQuizzes = subjects.size() * FREEMIUM_QUIZZES_PER_SUBJECT;

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
            if (quizzes.size() < FREEMIUM_QUIZZES_PER_SUBJECT && !allowPartial) {
                throw new InsufficientStockException(
                        "Insufficient freemium quizzes for subject " + subject.getName(),
                        quizzes.size(),
                        FREEMIUM_QUIZZES_PER_SUBJECT
                );
            }
            int take = Math.min(FREEMIUM_QUIZZES_PER_SUBJECT, quizzes.size());
            for (int i = 0; i < take; i++) {
                Quiz quiz = quizzes.get(i);
                int activeQs = questionRepository.findByQuizIdAndBlacklistedFalse(quiz.getId()).size();
                if (activeQs < FreemiumCatalogSeeder.QUESTIONS_PER_QUIZ && !allowPartial) {
                    throw new InsufficientStockException(
                            "Insufficient questions for quiz " + quiz.getTitle(),
                            activeQs,
                            FreemiumCatalogSeeder.QUESTIONS_PER_QUIZ
                    );
                }
                quizIds.add(quiz.getId());
            }
        }

        if (quizIds.isEmpty()) {
            throw new InsufficientStockException("No freemium quizzes available", 0, requiredQuizzes);
        }

        FreemiumPack issued = new FreemiumPack();
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
        return toResponse(freemiumPackRepository.save(issued));
    }

    private FreemiumPackResponse toResponse(FreemiumPack pack) {
        List<QuizResponse> quizzes = new ArrayList<>();
        for (Long quizId : pack.getQuizIds()) {
            quizRepository.findById(quizId).ifPresent(quiz -> quizzes.add(mapQuizWithQuestions(quiz)));
        }
        return new FreemiumPackResponse(
                pack.getId(),
                pack.getClassName(),
                pack.getLanguage(),
                pack.getBoardCode(),
                pack.getSubjects(),
                FREEMIUM_QUIZZES_PER_SUBJECT,
                pack.getQuizCount(),
                pack.getDeviceId(),
                pack.getChildId(),
                pack.getUserId(),
                quizzes,
                pack.getCreatedAt()
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

    private void validateHolder(FreemiumPackRequest request) {
        if (request.childId() == null && (request.deviceId() == null || request.deviceId().isBlank())) {
            throw new IllegalArgumentException("Either childId or deviceId is required");
        }
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
