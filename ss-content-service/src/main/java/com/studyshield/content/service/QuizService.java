package com.studyshield.content.service;

import com.studyshield.content.dto.QuestionResponse;
import com.studyshield.content.dto.QuizRequest;
import com.studyshield.content.dto.QuizResponse;
import com.studyshield.content.entity.ContentPack;
import com.studyshield.content.entity.ContentTier;
import com.studyshield.content.entity.Quiz;
import com.studyshield.content.entity.Quiz.QuizType;
import com.studyshield.content.exception.ResourceNotFoundException;
import com.studyshield.content.repository.ContentPackRepository;
import com.studyshield.content.repository.QuestionRepository;
import com.studyshield.content.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final ContentPackRepository contentPackRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;

    public QuizService(
            QuizRepository quizRepository,
            ContentPackRepository contentPackRepository,
            QuestionRepository questionRepository,
            QuestionService questionService
    ) {
        this.quizRepository = quizRepository;
        this.contentPackRepository = contentPackRepository;
        this.questionRepository = questionRepository;
        this.questionService = questionService;
    }

    public QuizResponse create(QuizRequest request) {
        ContentPack contentPack = contentPackRepository.findById(request.contentPackId())
                .orElseThrow(() -> new ResourceNotFoundException("ContentPack", request.contentPackId()));
        QuizType type = request.quizType() != null ? request.quizType() : QuizType.STANDARD;
        int count = resolveQuestionCount(request.questionCount(), type);
        Quiz quiz = Quiz.builder()
                .title(request.title())
                .description(request.description())
                .contentPack(contentPack)
                .quizType(type)
                .questionCount(count)
                .contentTier(request.contentTier() != null ? request.contentTier() : ContentTier.FREEMIUM)
                .freemiumIndex(request.freemiumIndex())
                .language(request.language() != null ? request.language() : "English")
                .active(request.active())
                .build();
        return mapToResponse(quizRepository.save(quiz), false);
    }

    @Transactional(readOnly = true)
    public QuizResponse getById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        return mapToResponse(quiz, true);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getAll() {
        return quizRepository.findAll().stream()
                .map(q -> mapToResponse(q, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getByContentPackId(Long contentPackId) {
        return quizRepository.findByContentPackId(contentPackId).stream()
                .map(q -> mapToResponse(q, false))
                .toList();
    }

    /**
     * Full freemium/premium download for a pack: each quiz includes active questions
     * (same shape the mobile app needs to cache and send over TCP).
     */
    @Transactional(readOnly = true)
    public List<QuizResponse> getByContentPackIdWithQuestions(Long contentPackId) {
        return quizRepository.findByContentPackId(contentPackId).stream()
                .filter(Quiz::isActive)
                .map(q -> mapToResponse(q, true))
                .toList();
    }

    public QuizResponse update(Long id, QuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        ContentPack contentPack = contentPackRepository.findById(request.contentPackId())
                .orElseThrow(() -> new ResourceNotFoundException("ContentPack", request.contentPackId()));
        QuizType type = request.quizType() != null ? request.quizType() : QuizType.STANDARD;
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());
        quiz.setContentPack(contentPack);
        quiz.setQuizType(type);
        quiz.setQuestionCount(resolveQuestionCount(request.questionCount(), type));
        quiz.setContentTier(request.contentTier() != null ? request.contentTier() : ContentTier.FREEMIUM);
        quiz.setFreemiumIndex(request.freemiumIndex());
        quiz.setLanguage(request.language() != null ? request.language() : "English");
        quiz.setActive(request.active());
        return mapToResponse(quizRepository.save(quiz), false);
    }

    public void delete(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
        quizRepository.delete(quiz);
    }

    private int resolveQuestionCount(Integer requested, QuizType type) {
        if (type == QuizType.SINGLE) {
            return 1;
        }
        if (requested != null && requested > 0) {
            return requested;
        }
        return 10;
    }

    private QuizResponse mapToResponse(Quiz quiz, boolean includeQuestions) {
        List<QuestionResponse> questions = null;
        if (includeQuestions) {
            questions = questionRepository.findByQuizIdAndBlacklistedFalse(quiz.getId()).stream()
                    .map(questionService::mapToResponse)
                    .toList();
        }
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
}
