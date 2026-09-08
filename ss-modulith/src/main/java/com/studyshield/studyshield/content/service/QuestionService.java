package com.studyshield.studyshield.content.service;

import com.studyshield.studyshield.content.dto.QuestionOptionDto;
import com.studyshield.studyshield.content.dto.QuestionRequest;
import com.studyshield.studyshield.content.dto.QuestionResponse;
import com.studyshield.studyshield.content.entity.*;
import com.studyshield.studyshield.common.exception.ResourceNotFoundException;
import com.studyshield.studyshield.content.repository.QuestionRepository;
import com.studyshield.studyshield.content.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                .versionGroupId(UUID.randomUUID().toString())
                .versionNumber(1)
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

    /**
     * Latest version of each question in the quiz. Older revisions of a question are kept in the
     * table but never served: the whole point of versioning is that a quiz always uses the newest.
     */
    @Transactional(readOnly = true)
    public List<QuestionResponse> getByQuizId(Long quizId) {
        return questionRepository.findByQuizIdAndSupersededByNull(quizId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getActiveByQuizId(Long quizId) {
        return questionRepository.findByQuizIdAndBlacklistedFalse(quizId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Full revision history of a question, oldest first. The head of the chain (last revision)
     * is the latest version currently served for its quiz.
     */
    @Transactional(readOnly = true)
    public List<QuestionResponse> getRevisions(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        String groupId = question.getVersionGroupId();
        if (groupId == null) {
            return List.of(mapToResponse(question));
        }
        return questionRepository.findByVersionGroupIdOrderByVersionNumberAsc(groupId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Editing an existing question creates a new version instead of mutating the row in place:
     * the previous latest version is superseded by the new row, and both share a version group.
     * Saving without any material change returns the current version untouched (no empty revision).
     */
    public QuestionResponse update(Long id, QuestionRequest request) {
        Question current = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", request.quizId()));
        validateShape(request);
        List<QuestionOption> options = toOptions(request.options());
        if (!hasMaterialChanges(current, request, options)) {
            return mapToResponse(current);
        }
        Question next = Question.builder()
                .resourceId(request.resourceId() != null ? request.resourceId() : current.getResourceId())
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
                .versionGroupId(current.getVersionGroupId() != null
                        ? current.getVersionGroupId() : UUID.randomUUID().toString())
                .versionNumber(current.getVersionNumber() + 1)
                .orderIndex(request.orderIndex())
                .build();
        Question saved = questionRepository.save(next);
        current.setSupersededBy(saved);
        questionRepository.save(current);
        return mapToResponse(saved);
    }

    /** Deletes the whole version group so no superseded revision is left orphaned. */
    public void delete(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
        String groupId = question.getVersionGroupId();
        if (groupId != null) {
            for (Question version : questionRepository.findByVersionGroupId(groupId)) {
                questionRepository.delete(version);
            }
        } else {
            questionRepository.delete(question);
        }
    }

    private boolean hasMaterialChanges(Question current, QuestionRequest request, List<QuestionOption> options) {
        if (current.getQuiz().getId() != null && request.quizId() != null
                && !current.getQuiz().getId().equals(request.quizId())) {
            return true;
        }
        if (!current.getQuestionText().equals(request.questionText())) {
            return true;
        }
        if (current.getQuestionType() != request.questionType()) {
            return true;
        }
        if (current.isBlacklisted() != request.blacklisted()) {
            return true;
        }
        if (current.getOrderIndex() != request.orderIndex()) {
            return true;
        }
        if (current.getPoints() != (request.points() != null && request.points() > 0 ? request.points() : 1)) {
            return true;
        }
        if (current.getDifficulty() != request.difficulty()) {
            return true;
        }
        List<String> currentCorrect = current.getCorrectAnswers() != null
                ? current.getCorrectAnswers() : new ArrayList<>();
        List<String> requestedCorrect = request.correctAnswers() != null
                ? request.correctAnswers() : new ArrayList<>();
        if (!currentCorrect.equals(requestedCorrect)) {
            return true;
        }
        List<QuestionOption> currentOptions = current.getOptions() != null
                ? current.getOptions() : new ArrayList<>();
        if (currentOptions.size() != options.size()) {
            return true;
        }
        for (int i = 0; i < options.size(); i++) {
            QuestionOption cur = currentOptions.get(i);
            QuestionOption req = options.get(i);
            if (!java.util.Objects.equals(cur.getId(), req.getId())
                    || !java.util.Objects.equals(cur.getText(), req.getText())
                    || !java.util.Objects.equals(cur.getImageUrl(), req.getImageUrl())) {
                return true;
            }
        }
        return false;
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
                question.getVersionGroupId(),
                question.getVersionNumber(),
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