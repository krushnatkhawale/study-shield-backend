package com.studyshield.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "child_profiles")
public class ChildProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long boardId;

    private Long classGradeId;

    private String gender;

    private Integer birthYear;

    private String studentClass;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ChildProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
    public Long getClassGradeId() { return classGradeId; }
    public void setClassGradeId(Long classGradeId) { this.classGradeId = classGradeId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }
    public String getStudentClass() { return studentClass; }
    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private int age;
        private User user;
        private Long boardId;
        private Long classGradeId;
        private String gender;
        private Integer birthYear;
        private String studentClass;
        private boolean active = true;

        public Builder name(String name) { this.name = name; return this; }
        public Builder age(int age) { this.age = age; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder boardId(Long boardId) { this.boardId = boardId; return this; }
        public Builder classGradeId(Long classGradeId) { this.classGradeId = classGradeId; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder birthYear(Integer birthYear) { this.birthYear = birthYear; return this; }
        public Builder studentClass(String studentClass) { this.studentClass = studentClass; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public ChildProfile build() {
            ChildProfile cp = new ChildProfile();
            cp.name = this.name;
            cp.age = this.age;
            cp.user = this.user;
            cp.boardId = this.boardId;
            cp.classGradeId = this.classGradeId;
            cp.gender = this.gender;
            cp.birthYear = this.birthYear;
            cp.studentClass = this.studentClass;
            cp.active = this.active;
            return cp;
        }
    }
}
