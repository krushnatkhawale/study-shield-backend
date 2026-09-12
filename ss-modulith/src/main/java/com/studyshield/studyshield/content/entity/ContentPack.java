package com.studyshield.studyshield.content.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "content_packs")
public class ContentPack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "pack_type", length = 32)
    private ContentTier packType = ContentTier.FREEMIUM;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @OneToMany(mappedBy = "contentPack", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Quiz> quizzes = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ContentPack() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public ContentTier getPackType() { return packType != null ? packType : ContentTier.FREEMIUM; }
    public void setPackType(ContentTier packType) { this.packType = packType; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public List<Quiz> getQuizzes() { return quizzes; }
    public void setQuizzes(List<Quiz> quizzes) { this.quizzes = quizzes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String description;
        private Subject subject;
        private int version = 1;
        private boolean active = true;
        private ContentTier packType = ContentTier.FREEMIUM;
        private LocalDate validFrom;
        private LocalDate validTo;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder subject(Subject subject) { this.subject = subject; return this; }
        public Builder version(int version) { this.version = version; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder packType(ContentTier packType) { this.packType = packType; return this; }
        public Builder validFrom(LocalDate validFrom) { this.validFrom = validFrom; return this; }
        public Builder validTo(LocalDate validTo) { this.validTo = validTo; return this; }

        public ContentPack build() {
            ContentPack cp = new ContentPack();
            cp.name = this.name;
            cp.description = this.description;
            cp.subject = this.subject;
            cp.version = this.version;
            cp.active = this.active;
            cp.packType = this.packType;
            cp.validFrom = this.validFrom;
            cp.validTo = this.validTo;
            return cp;
        }
    }
}
