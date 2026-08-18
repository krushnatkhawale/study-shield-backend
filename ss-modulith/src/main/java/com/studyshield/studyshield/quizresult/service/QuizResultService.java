package com.studyshield.studyshield.quizresult.service;

import com.studyshield.studyshield.quizresult.dto.QuizResultListItem;
import com.studyshield.studyshield.quizresult.dto.QuizResultRequest;
import com.studyshield.studyshield.quizresult.dto.QuizResultResponse;
import com.studyshield.studyshield.quizresult.entity.QuizResult;
import com.studyshield.studyshield.quizresult.repository.QuizResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class QuizResultService {

    private final QuizResultRepository quizResultRepository;

    public QuizResultService(QuizResultRepository quizResultRepository) {
        this.quizResultRepository = quizResultRepository;
    }

    public QuizResultResponse save(QuizResultRequest request) {
        LocalDateTime completedAt = Instant.ofEpochMilli(request.completedAt())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        QuizResult quizResult = QuizResult.builder()
                .childName(request.childName())
                .score(request.score())
                .totalQuestions(request.totalQuestions())
                .timeSpentSeconds(request.timeSpentSeconds())
                .contentName(request.contentName())
                .category(request.category())
                .completedAt(completedAt)
                .build();

        QuizResult saved = quizResultRepository.save(quizResult);
        return QuizResultResponse.success(String.valueOf(saved.getId()));
    }

    @Transactional(readOnly = true)
    public List<QuizResultListItem> listAll() {
        return quizResultRepository.findAllByOrderByCompletedAtDesc().stream()
                .map(this::mapToListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResultListItem> listByChildName(String childName) {
        return quizResultRepository.findByChildNameOrderByCompletedAtDesc(childName).stream()
                .map(this::mapToListItem)
                .toList();
    }

    private QuizResultListItem mapToListItem(QuizResult qr) {
        return new QuizResultListItem(
                qr.getId(),
                qr.getChildName(),
                qr.getScore(),
                qr.getTotalQuestions(),
                qr.getTimeSpentSeconds(),
                qr.getContentName(),
                qr.getCategory(),
                qr.getCompletedAt(),
                qr.getCreatedAt()
        );
    }
}
