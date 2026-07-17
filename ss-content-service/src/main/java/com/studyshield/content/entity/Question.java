package com.studyshield.content.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quiz question aligned with mobile master data ({@code QuizFileQuestion} / assets JSON).
 * <p>
 * Stored under a {@link Quiz} for freemium/content-pack delivery (app plays ~10 questions per quiz).
 * Options and correct answers use option ids (not A/B/C/D columns) so SINGLE_CHOICE,
 * MULTIPLE_CHOICE, TRUE_FALSE, and FITB all share one structure.
 */
@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_questions_resource_id", columnList = "resource_id"),
        @Index(name = "idx_questions_quiz_id", columnList = "quiz_id")
})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable key from assets, e.g. {@code c01_q01}, {@code nur_evs_01}. */
    @Column(name = "resource_id", length = 64)
    private String resourceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_image_url", columnDefinition = "TEXT")
    private String questionImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 32)
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;

    /** Legacy column — kept to satisfy NOT NULL constraint on existing DB schema. */
    @Column(name = "correct_option", nullable = false, length = 4)
    private String correctOption = "A";

    /** Legacy columns — kept to satisfy NOT NULL constraints on existing DB schema. */
    @Column(name = "optiona", nullable = false, length = 255)
    private String optionA = "";

    @Column(name = "optionb", nullable = false, length = 255)
    private String optionB = "";

    @Column(name = "optionc", nullable = false, length = 255)
    private String optionC = "";

    @Column(name = "optiond", nullable = false, length = 255)
    private String optionD = "";

    @Column(name = "optiona_image", length = 500)
    private String optionAImage;

    @Column(name = "optionb_image", length = 500)
    private String optionBImage;

    @Column(name = "optionc_image", length = 500)
    private String optionCImage;

    @Column(name = "optiond_image", length = 500)
    private String optionDImage;

    /**
     * JSON array of {@link QuestionOption}. Empty list allowed for FITB.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private List<QuestionOption> options = new ArrayList<>();

    /**
     * Option ids for choice types, or accepted free-text answers for FITB.
     * Matches mobile {@code correctAnswers}.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "correct_answers", nullable = false, columnDefinition = "json")
    private List<String> correctAnswers = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private int points = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Difficulty difficulty = Difficulty.EASY;

    /** e.g. ["English", "Hindi"] — matches mobile {@code languages}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> languages = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> tags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    /** Soft-retire; active quiz fetch excludes blacklisted. */
    @Column(nullable = false)
    private boolean blacklisted = false;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Question() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getQuestionImageUrl() { return questionImageUrl; }
    public void setQuestionImageUrl(String questionImageUrl) { this.questionImageUrl = questionImageUrl; }
    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getOptionAImage() { return optionAImage; }
    public void setOptionAImage(String optionAImage) { this.optionAImage = optionAImage; }
    public String getOptionBImage() { return optionBImage; }
    public void setOptionBImage(String optionBImage) { this.optionBImage = optionBImage; }
    public String getOptionCImage() { return optionCImage; }
    public void setOptionCImage(String optionCImage) { this.optionCImage = optionCImage; }
    public String getOptionDImage() { return optionDImage; }
    public void setOptionDImage(String optionDImage) { this.optionDImage = optionDImage; }
    public List<QuestionOption> getOptions() { return options; }
    public void setOptions(List<QuestionOption> options) { this.options = options != null ? options : new ArrayList<>(); }
    public List<String> getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers != null ? correctAnswers : new ArrayList<>();
    }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages != null ? languages : new ArrayList<>(); }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public boolean isBlacklisted() { return blacklisted; }
    public void setBlacklisted(boolean blacklisted) { this.blacklisted = blacklisted; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String resourceId;
        private String questionText;
        private String questionImageUrl;
        private QuestionType questionType = QuestionType.SINGLE_CHOICE;
        private String correctOption = "A";
        private String optionA = "";
        private String optionB = "";
        private String optionC = "";
        private String optionD = "";
        private String optionAImage;
        private String optionBImage;
        private String optionCImage;
        private String optionDImage;
        private List<QuestionOption> options = new ArrayList<>();
        private List<String> correctAnswers = new ArrayList<>();
        private String explanation;
        private int points = 1;
        private Difficulty difficulty = Difficulty.EASY;
        private List<String> languages = new ArrayList<>();
        private List<String> tags = new ArrayList<>();
        private Quiz quiz;
        private boolean blacklisted = false;
        private int orderIndex;

        public Builder resourceId(String resourceId) { this.resourceId = resourceId; return this; }
        public Builder questionText(String questionText) { this.questionText = questionText; return this; }
        public Builder questionImageUrl(String questionImageUrl) { this.questionImageUrl = questionImageUrl; return this; }
        public Builder questionType(QuestionType questionType) { this.questionType = questionType; return this; }
        public Builder correctOption(String correctOption) { this.correctOption = correctOption; return this; }
        public Builder optionA(String optionA) { this.optionA = optionA; return this; }
        public Builder optionB(String optionB) { this.optionB = optionB; return this; }
        public Builder optionC(String optionC) { this.optionC = optionC; return this; }
        public Builder optionD(String optionD) { this.optionD = optionD; return this; }
        public Builder optionAImage(String optionAImage) { this.optionAImage = optionAImage; return this; }
        public Builder optionBImage(String optionBImage) { this.optionBImage = optionBImage; return this; }
        public Builder optionCImage(String optionCImage) { this.optionCImage = optionCImage; return this; }
        public Builder optionDImage(String optionDImage) { this.optionDImage = optionDImage; return this; }
        public Builder options(List<QuestionOption> options) { this.options = options; return this; }
        public Builder correctAnswers(List<String> correctAnswers) { this.correctAnswers = correctAnswers; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder points(int points) { this.points = points; return this; }
        public Builder difficulty(Difficulty difficulty) { this.difficulty = difficulty; return this; }
        public Builder languages(List<String> languages) { this.languages = languages; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder quiz(Quiz quiz) { this.quiz = quiz; return this; }
        public Builder blacklisted(boolean blacklisted) { this.blacklisted = blacklisted; return this; }
        public Builder orderIndex(int orderIndex) { this.orderIndex = orderIndex; return this; }

        public Question build() {
            Question q = new Question();
            q.resourceId = this.resourceId;
            q.questionText = this.questionText;
            q.questionImageUrl = this.questionImageUrl;
            q.questionType = this.questionType != null ? this.questionType : QuestionType.SINGLE_CHOICE;
            q.correctOption = this.correctOption != null ? this.correctOption : "A";
            q.optionA = this.optionA != null ? this.optionA : "";
            q.optionB = this.optionB != null ? this.optionB : "";
            q.optionC = this.optionC != null ? this.optionC : "";
            q.optionD = this.optionD != null ? this.optionD : "";
            q.optionAImage = this.optionAImage;
            q.optionBImage = this.optionBImage;
            q.optionCImage = this.optionCImage;
            q.optionDImage = this.optionDImage;
            q.options = this.options != null ? this.options : new ArrayList<>();
            q.correctAnswers = this.correctAnswers != null ? this.correctAnswers : new ArrayList<>();
            q.explanation = this.explanation;
            q.points = this.points;
            q.difficulty = this.difficulty != null ? this.difficulty : Difficulty.EASY;
            q.languages = this.languages != null ? this.languages : new ArrayList<>();
            q.tags = this.tags != null ? this.tags : new ArrayList<>();
            q.quiz = this.quiz;
            q.blacklisted = this.blacklisted;
            q.orderIndex = this.orderIndex;
            return q;
        }
    }
}
