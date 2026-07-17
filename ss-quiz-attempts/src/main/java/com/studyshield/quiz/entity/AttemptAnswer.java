package com.studyshield.quiz.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false)
    private QuizAttempt quizAttempt;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private String selectedOption;

    private boolean correct;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public AttemptAnswer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public QuizAttempt getQuizAttempt() { return quizAttempt; }
    public void setQuizAttempt(QuizAttempt quizAttempt) { this.quizAttempt = quizAttempt; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private QuizAttempt quizAttempt;
        private Long questionId;
        private String selectedOption;
        private boolean correct;

        public Builder quizAttempt(QuizAttempt qa) { this.quizAttempt = qa; return this; }
        public Builder questionId(Long qid) { this.questionId = qid; return this; }
        public Builder selectedOption(String opt) { this.selectedOption = opt; return this; }
        public Builder correct(boolean c) { this.correct = c; return this; }

        public AttemptAnswer build() {
            AttemptAnswer aa = new AttemptAnswer();
            aa.quizAttempt = this.quizAttempt;
            aa.questionId = this.questionId;
            aa.selectedOption = this.selectedOption;
            aa.correct = this.correct;
            return aa;
        }
    }
}
