package com.studyshield.studyshield.content.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "class_levels")
public class ClassLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer ordinal;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Column(name = "age_min_years")
    private Double ageMinYears;

    @Column(name = "age_max_years")
    private Double ageMaxYears;

    private String stage;
    private String notes;

    public ClassLevel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer ordinal) { this.ordinal = ordinal; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }
    public Double getAgeMinYears() { return ageMinYears; }
    public void setAgeMinYears(Double ageMinYears) { this.ageMinYears = ageMinYears; }
    public Double getAgeMaxYears() { return ageMaxYears; }
    public void setAgeMaxYears(Double ageMaxYears) { this.ageMaxYears = ageMaxYears; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
