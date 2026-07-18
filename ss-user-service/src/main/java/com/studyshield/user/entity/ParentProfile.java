package com.studyshield.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "parent_profiles")
public class ParentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String gender;

    private String relation;

    @Column(nullable = false)
    private String type = "ACCOUNT_HOLDER";

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ParentProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long userId;
        private String name;
        private String gender;
        private String relation;
        private String type = "ACCOUNT_HOLDER";
        private boolean isDefault = false;
        private boolean active = true;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder relation(String relation) { this.relation = relation; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder isDefault(boolean isDefault) { this.isDefault = isDefault; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public ParentProfile build() {
            ParentProfile p = new ParentProfile();
            p.userId = this.userId;
            p.name = this.name;
            p.gender = this.gender;
            p.relation = this.relation;
            p.type = this.type;
            p.isDefault = this.isDefault;
            p.active = this.active;
            return p;
        }
    }
}
