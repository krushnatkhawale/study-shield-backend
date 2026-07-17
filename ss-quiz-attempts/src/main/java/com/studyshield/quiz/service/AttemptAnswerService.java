package com.studyshield.quiz.service;

import com.studyshield.quiz.dto.AttemptAnswerRequest;
import com.studyshield.quiz.dto.AttemptAnswerResponse;
import com.studyshield.quiz.entity.AttemptAnswer;
import com.studyshield.quiz.entity.QuizAttempt;
import com.studyshield.quiz.exception.ResourceNotFoundException;
import com.studyshield.quiz.repository.AttemptAnswerRepository;
import com.studyshield.quiz.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AttemptAnswerService {

    private final AttemptAnswerRepository attemptAnswerRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public AttemptAnswerService(AttemptAnswerRepository attemptAnswerRepository, QuizAttemptRepository quizAttemptRepository) {
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public AttemptAnswerResponse create(Long quizAttemptId, AttemptAnswerRequest request) {
        QuizAttempt qa = quizAttemptRepository.findById(quizAttemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", quizAttemptId));
        AttemptAnswer aa = AttemptAnswer.builder()
                .quizAttempt(qa)
                .questionId(request.questionId())
                .selectedOption(request.selectedOption())
                .build();
        return mapToResponse(attemptAnswerRepository.save(aa));
    }

    @Transactional(readOnly = true)
    public List<AttemptAnswerResponse> getByQuizAttemptId(Long quizAttemptId) {
        return attemptAnswerRepository.findByQuizAttemptId(quizAttemptId).stream().map(this::mapToResponse).toList();
    }

    private AttemptAnswerResponse mapToResponse(AttemptAnswer aa) {
        return new AttemptAnswerResponse(aa.getId(), aa.getQuizAttempt().getId(),
                aa.getQuestionId(), aa.getSelectedOption(), aa.isCorrect(), aa.getCreatedAt());
    }
}
