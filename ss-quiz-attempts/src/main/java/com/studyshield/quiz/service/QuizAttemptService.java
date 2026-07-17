package com.studyshield.quiz.service;

import com.studyshield.quiz.dto.QuizAttemptRequest;
import com.studyshield.quiz.dto.QuizAttemptResponse;
import com.studyshield.quiz.entity.QuizAttempt;
import com.studyshield.quiz.exception.ResourceNotFoundException;
import com.studyshield.quiz.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public QuizAttemptResponse create(QuizAttemptRequest request) {
        QuizAttempt qa = QuizAttempt.builder()
                .quizId(request.quizId())
                .childProfileId(request.childProfileId())
                .userId(request.userId())
                .totalQuestions(request.totalQuestions())
                .build();
        return mapToResponse(quizAttemptRepository.save(qa));
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponse getById(Long id) {
        return mapToResponse(quizAttemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", id)));
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getByUserId(Long userId) {
        return quizAttemptRepository.findByUserId(userId).stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getByChildProfileId(Long childProfileId) {
        return quizAttemptRepository.findByChildProfileId(childProfileId).stream().map(this::mapToResponse).toList();
    }

    public QuizAttemptResponse complete(Long id, int correctAnswers) {
        QuizAttempt qa = quizAttemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", id));
        qa.setStatus(QuizAttempt.AttemptStatus.COMPLETED);
        qa.setCorrectAnswers(correctAnswers);
        if (qa.getTotalQuestions() != null && qa.getTotalQuestions() > 0) {
            qa.setScore((correctAnswers * 100) / qa.getTotalQuestions());
        }
        return mapToResponse(quizAttemptRepository.save(qa));
    }

    public void delete(Long id) {
        QuizAttempt qa = quizAttemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", id));
        quizAttemptRepository.delete(qa);
    }

    private QuizAttemptResponse mapToResponse(QuizAttempt qa) {
        return new QuizAttemptResponse(qa.getId(), qa.getQuizId(), qa.getChildProfileId(),
                qa.getUserId(), qa.getStatus().name(), qa.getTotalQuestions(),
                qa.getCorrectAnswers(), qa.getScore(), qa.getCreatedAt(), qa.getUpdatedAt());
    }
}
