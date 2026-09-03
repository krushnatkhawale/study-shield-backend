package com.studyshield.studyshield.feedback.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Review feedback a user gives on a single question: an up/down vote (once per
 * user, reversible), an optional downs-specific category and comment, and a
 * separate report flag (report always carries a mandatory comment).
 * <p>
 * One row per (account, question); upserted on each submission so repeated taps
 * update rather than duplicate.
 */
@Entity
@Table(name = "question_feedback", uniqueConstraints = {
        @UniqueConstraint(name = "uk_question_feedback_account_question", columnNames = {"account_id", "question_id"})
}, indexes = {
        @Index(name = "idx_question_feedback_question_id", columnList = "question_id")
})
public class QuestionFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FeedbackVote vote = FeedbackVote.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "down_category", length = 32)
    private DownCategory downCategory;

    @Column(nullable = false)
    private boolean reported = false;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public QuestionFeedback() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public FeedbackVote getVote() { return vote; }
    public void setVote(FeedbackVote vote) { this.vote = vote; }
    public DownCategory getDownCategory() { return downCategory; }
    public void setDownCategory(DownCategory downCategory) { this.downCategory = downCategory; }
    public boolean isReported() { return reported; }
    public void setReported(boolean reported) { this.reported = reported; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
