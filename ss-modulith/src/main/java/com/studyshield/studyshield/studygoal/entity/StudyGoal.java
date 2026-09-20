package com.studyshield.studyshield.studygoal.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "goal")
public class StudyGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    @Column(nullable = false)
    private String childName;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer target;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public StudyGoal() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getTarget() { return target; }
    public void setTarget(Integer target) { this.target = target; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long accountId;
        private String childName;
        private String type;
        private Integer target;
        private boolean active = true;

        public Builder accountId(Long accountId) { this.accountId = accountId; return this; }
        public Builder childName(String childName) { this.childName = childName; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder target(Integer target) { this.target = target; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public StudyGoal build() {
            StudyGoal g = new StudyGoal();
            g.accountId = this.accountId;
            g.childName = this.childName;
            g.type = this.type;
            g.target = this.target;
            g.active = this.active;
            return g;
        }
    }
}
