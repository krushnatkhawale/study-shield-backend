package com.studyshield.quiz.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    public enum AttemptStatus {
        IN_PROGRESS,
        COMPLETED,
        ABANDONED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quizId;

    @Column(nullable = false)
    private Long childProfileId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    private Integer totalQuestions;

    private Integer correctAnswers;

    private Integer score;

    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AttemptAnswer> answers = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public QuizAttempt() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public Long getChildProfileId() { return childProfileId; }
    public void setChildProfileId(Long childProfileId) { this.childProfileId = childProfileId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public List<AttemptAnswer> getAnswers() { return answers; }
    public void setAnswers(List<AttemptAnswer> answers) { this.answers = answers; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long quizId;
        private Long childProfileId;
        private Long userId;
        private AttemptStatus status = AttemptStatus.IN_PROGRESS;
        private Integer totalQuestions;

        public Builder quizId(Long quizId) { this.quizId = quizId; return this; }
        public Builder childProfileId(Long childProfileId) { this.childProfileId = childProfileId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder status(AttemptStatus status) { this.status = status; return this; }
        public Builder totalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; return this; }

        public QuizAttempt build() {
            QuizAttempt qa = new QuizAttempt();
            qa.quizId = this.quizId;
            qa.childProfileId = this.childProfileId;
            qa.userId = this.userId;
            qa.status = this.status;
            qa.totalQuestions = this.totalQuestions;
            return qa;
        }
    }
}
