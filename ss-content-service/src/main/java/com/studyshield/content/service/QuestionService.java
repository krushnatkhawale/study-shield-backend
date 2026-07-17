package com.studyshield.content.service;

import com.studyshield.content.dto.QuestionOptionDto;
import com.studyshield.content.dto.QuestionRequest;
import com.studyshield.content.dto.QuestionResponse;
import com.studyshield.content.entity.*;
import com.studyshield.content.exception.ResourceNotFoundException;
import com.studyshield.content.repository.QuestionRepository;
import com.studyshield.content.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public QuestionResponse create(QuestionRequest request) {
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", request.quizId()));
        validateShape(request);
        List<QuestionOption> options = toOptions(request.options());
        Question question = Question.builder()
                .resourceId(request.resourceId())
                .questionText(request.questionText())
                .questionImageUrl(request.questionImageUrl())
                .questionType(request.questionType())
                .correctOption(request.correctAnswers() != null && !request.correctAnswers().isEmpty()
                        ? request.correctAnswers().get(0) : "A")
                .optionA(getOptionText(options, 0))
                .optionB(getOptionText(options, 1))
                .optionC(getOptionText(options, 2))
                .optionD(getOptionText(options, 3))
                .optionAImage(getOptionImage(options, 0))
                .optionBImage(getOptionImage(options, 1))
                .optionCImage(getOptionImage(options, 2))
                .optionDImage(getOptionImage(options, 3))
                .options(options)
                .correctAnswers(new ArrayList<>(request.correctAnswers()))
                .explanation(request.explanation())
                .points(request.points() != null && request.points() > 0 ? request.points() : 1)
                .difficulty(request.difficulty() != null ? request.difficulty() : Difficulty.EASY)
                .languages(request.languages() != null ? new ArrayList<>(request.languages()) : List.of("English"))
                .tags(request.tags() != null ? new ArrayList<>(request.tags()) : new ArrayList<>())
                .quiz(quiz)
                .blacklisted(request.blacklisted())
                .orderIndex(request.orderIndex())
                .build();
        return mapToResponse(questionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public QuestionResponse getById(Long id) {
        return mapToResponse(questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id)));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getAll() {
        return questionRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getByQuizId(Long quizId) {
        return questionRepository.findByQuizId(quizId).stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getActiveByQuizId(Long quizId) {
        return questionRepository.findByQuizIdAndBlacklistedFalse(quizId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public QuestionResponse update(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", request.quizId()));
        validateShape(request);
        List<QuestionOption> options = toOptions(request.options());
        question.setResourceId(request.resourceId());
        question.setQuestionText(request.questionText());
        question.setQuestionImageUrl(request.questionImageUrl());
        question.setQuestionType(request.questionType());
        question.setCorrectOption(request.correctAnswers() != null && !request.correctAnswers().isEmpty()
                ? request.correctAnswers().get(0) : "A");
        question.setOptionA(getOptionText(options, 0));
        question.setOptionB(getOptionText(options, 1));
        question.setOptionC(getOptionText(options, 2));
        question.setOptionD(getOptionText(options, 3));
        question.setOptionAImage(getOptionImage(options, 0));
        question.setOptionBImage(getOptionImage(options, 1));
        question.setOptionCImage(getOptionImage(options, 2));
        question.setOptionDImage(getOptionImage(options, 3));
        question.setOptions(options);
        question.setCorrectAnswers(new ArrayList<>(request.correctAnswers()));
        question.setExplanation(request.explanation());
        question.setPoints(request.points() != null && request.points() > 0 ? request.points() : 1);
        question.setDifficulty(request.difficulty() != null ? request.difficulty() : Difficulty.EASY);
        question.setLanguages(request.languages() != null ? new ArrayList<>(request.languages()) : List.of("English"));
        question.setTags(request.tags() != null ? new ArrayList<>(request.tags()) : new ArrayList<>());
        question.setQuiz(quiz);
        question.setBlacklisted(request.blacklisted());
        question.setOrderIndex(request.orderIndex());
        return mapToResponse(questionRepository.save(question));
    }

    public void delete(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        questionRepository.delete(question);
    }

    private void validateShape(QuestionRequest request) {
        QuestionType type = request.questionType();
        List<QuestionOptionDto> options = request.options() != null ? request.options() : List.of();
        if (type == QuestionType.FITB) {
            return;
        }
        if (options.isEmpty()) {
            throw new IllegalArgumentException("Options are required for question type " + type);
        }
        if (type == QuestionType.TRUE_FALSE && options.size() != 2) {
            throw new IllegalArgumentException("TRUE_FALSE questions must have exactly 2 options");
        }
        if (type == QuestionType.SINGLE_CHOICE && request.correctAnswers().size() != 1) {
            throw new IllegalArgumentException("SINGLE_CHOICE requires exactly one correct answer id");
        }
    }

    private List<QuestionOption> toOptions(List<QuestionOptionDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(d -> new QuestionOption(d.id(), d.text(), d.imageUrl()))
                .toList();
    }

    private String getOptionText(List<QuestionOption> options, int index) {
        if (options != null && index < options.size()) {
            return options.get(index).getText() != null ? options.get(index).getText() : "";
        }
        return "";
    }

    private String getOptionImage(List<QuestionOption> options, int index) {
        if (options != null && index < options.size()) {
            return options.get(index).getImageUrl();
        }
        return null;
    }

    QuestionResponse mapToResponse(Question question) {
        List<QuestionOptionDto> optionDtos = question.getOptions() == null
                ? List.of()
                : question.getOptions().stream()
                .map(o -> new QuestionOptionDto(o.getId(), o.getText(), o.getImageUrl()))
                .toList();
        return new QuestionResponse(
                question.getId(),
                question.getResourceId(),
                question.getQuestionText(),
                question.getQuestionImageUrl(),
                question.getQuestionType(),
                optionDtos,
                question.getCorrectAnswers() != null ? question.getCorrectAnswers() : List.of(),
                question.getExplanation(),
                question.getPoints(),
                question.getDifficulty(),
                question.getLanguages() != null ? question.getLanguages() : List.of(),
                question.getTags() != null ? question.getTags() : List.of(),
                question.getQuiz().getId(),
                question.isBlacklisted(),
                question.getOrderIndex(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
