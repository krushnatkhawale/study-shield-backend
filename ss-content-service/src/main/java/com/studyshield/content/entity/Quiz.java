package com.studyshield.content.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A playable quiz unit for the mobile app (typically 10 questions).
 * <p>
 * Freemium: content pack per subject holds up to 5 quizzes ({@link #freemiumIndex} 1..5).
 * STANDARD = multi-question interrupt; SINGLE = one-question mode for younger kids.
 * Matches product brainstorming: mobile downloads quizzes, never the full bank.
 */
@Entity
@Table(name = "quizzes", indexes = {
        @Index(name = "idx_quizzes_content_pack_id", columnList = "content_pack_id")
})
public class Quiz {

    public enum QuizType {
        /** Default interrupt quiz size (app: 10 questions). */
        STANDARD,
        /** Single-question mode for young learners. */
        SINGLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_pack_id", nullable = false)
    private ContentPack contentPack;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private QuizType quizType = QuizType.STANDARD;

    /** Expected question count; STANDARD defaults to 10 (app behaviour). */
    @Column(name = "question_count", nullable = false)
    private int questionCount = 10;

    /** FREEMIUM pack slot vs PREMIUM on-demand / LIBRARY catalog. */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_tier", nullable = false, length = 16)
    private ContentTier contentTier = ContentTier.FREEMIUM;

    /**
     * Position within freemium set for a subject (1..5). Null if not freemium-slot based.
     */
    @Column(name = "freemium_index")
    private Integer freemiumIndex;

    /** Primary language of this quiz payload (app filter). */
    @Column(nullable = false, length = 64)
    private String language = "English";

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<Question> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Quiz() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ContentPack getContentPack() { return contentPack; }
    public void setContentPack(ContentPack contentPack) { this.contentPack = contentPack; }
    public QuizType getQuizType() { return quizType; }
    public void setQuizType(QuizType quizType) { this.quizType = quizType; }
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public ContentTier getContentTier() { return contentTier; }
    public void setContentTier(ContentTier contentTier) { this.contentTier = contentTier; }
    public Integer getFreemiumIndex() { return freemiumIndex; }
    public void setFreemiumIndex(Integer freemiumIndex) { this.freemiumIndex = freemiumIndex; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private String description;
        private ContentPack contentPack;
        private QuizType quizType = QuizType.STANDARD;
        private int questionCount = 10;
        private ContentTier contentTier = ContentTier.FREEMIUM;
        private Integer freemiumIndex;
        private String language = "English";
        private boolean active = true;

        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder contentPack(ContentPack contentPack) { this.contentPack = contentPack; return this; }
        public Builder quizType(QuizType quizType) { this.quizType = quizType; return this; }
        public Builder questionCount(int questionCount) { this.questionCount = questionCount; return this; }
        public Builder contentTier(ContentTier contentTier) { this.contentTier = contentTier; return this; }
        public Builder freemiumIndex(Integer freemiumIndex) { this.freemiumIndex = freemiumIndex; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public Quiz build() {
            Quiz q = new Quiz();
            q.title = this.title;
            q.description = this.description;
            q.contentPack = this.contentPack;
            q.quizType = this.quizType != null ? this.quizType : QuizType.STANDARD;
            q.questionCount = this.questionCount > 0 ? this.questionCount : 10;
            q.contentTier = this.contentTier != null ? this.contentTier : ContentTier.FREEMIUM;
            q.freemiumIndex = this.freemiumIndex;
            q.language = this.language != null && !this.language.isBlank() ? this.language : "English";
            q.active = this.active;
            return q;
        }
    }
}
