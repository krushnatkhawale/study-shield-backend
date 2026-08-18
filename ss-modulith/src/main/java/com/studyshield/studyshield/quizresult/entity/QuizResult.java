package com.studyshield.studyshield.quizresult.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String childName;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Long timeSpentSeconds;

    private String contentName;

    private String category;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public QuizResult() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Long getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Long timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }
    public String getContentName() { return contentName; }
    public void setContentName(String contentName) { this.contentName = contentName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String childName;
        private Integer score;
        private Integer totalQuestions;
        private Long timeSpentSeconds;
        private String contentName;
        private String category;
        private LocalDateTime completedAt;

        public Builder childName(String childName) { this.childName = childName; return this; }
        public Builder score(Integer score) { this.score = score; return this; }
        public Builder totalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; return this; }
        public Builder timeSpentSeconds(Long timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; return this; }
        public Builder contentName(String contentName) { this.contentName = contentName; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }

        public QuizResult build() {
            QuizResult qr = new QuizResult();
            qr.childName = this.childName;
            qr.score = this.score;
            qr.totalQuestions = this.totalQuestions;
            qr.timeSpentSeconds = this.timeSpentSeconds;
            qr.contentName = this.contentName;
            qr.category = this.category;
            qr.completedAt = this.completedAt;
            return qr;
        }
    }
}
