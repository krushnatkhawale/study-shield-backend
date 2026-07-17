package com.studyshield.content.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_grade_id", nullable = false)
    private ClassGrade classGrade;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ContentPack> contentPacks = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Subject() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ClassGrade getClassGrade() { return classGrade; }
    public void setClassGrade(ClassGrade classGrade) { this.classGrade = classGrade; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<ContentPack> getContentPacks() { return contentPacks; }
    public void setContentPacks(List<ContentPack> contentPacks) { this.contentPacks = contentPacks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String code;
        private String description;
        private ClassGrade classGrade;
        private boolean active = true;

        public Builder name(String name) { this.name = name; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder classGrade(ClassGrade classGrade) { this.classGrade = classGrade; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public Subject build() {
            Subject s = new Subject();
            s.name = this.name;
            s.code = this.code;
            s.description = this.description;
            s.classGrade = this.classGrade;
            s.active = this.active;
            return s;
        }
    }
}
