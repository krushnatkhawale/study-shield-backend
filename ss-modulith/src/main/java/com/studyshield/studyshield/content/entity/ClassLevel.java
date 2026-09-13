package com.studyshield.studyshield.content.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The global age-band spine (ordinals 1..17). One row per ordinal across all boards:
 * identity is the ordinal, never the display name. Display names are board-local and
 * live on {@link BoardClass}.
 */
@Entity
@Table(name = "class_levels", uniqueConstraints = {
        @UniqueConstraint(name = "uk_class_levels_ordinal", columnNames = "ordinal"),
        @UniqueConstraint(name = "uk_class_levels_slug", columnNames = "slug")
})
public class ClassLevel {

    public enum Stage {
        EARLY_YEARS,
        PRIMARY,
        LOWER_SECONDARY,
        UPPER_SECONDARY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int ordinal;

    @Column(nullable = false, length = 64)
    private String slug;

    @Column(name = "canonical_name", nullable = false, length = 64)
    private String canonicalName;

    @Column(name = "age_min_years", nullable = false, precision = 4, scale = 1)
    private BigDecimal ageMinYears;

    @Column(name = "age_max_years", nullable = false, precision = 4, scale = 1)
    private BigDecimal ageMaxYears;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Stage stage;

    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ClassLevel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getOrdinal() { return ordinal; }
    public void setOrdinal(int ordinal) { this.ordinal = ordinal; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }
    public BigDecimal getAgeMinYears() { return ageMinYears; }
    public void setAgeMinYears(BigDecimal ageMinYears) { this.ageMinYears = ageMinYears; }
    public BigDecimal getAgeMaxYears() { return ageMaxYears; }
    public void setAgeMaxYears(BigDecimal ageMaxYears) { this.ageMaxYears = ageMaxYears; }
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}