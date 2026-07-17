package com.studyshield.content.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class_grades")
public class ClassGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int gradeNumber;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @OneToMany(mappedBy = "classGrade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subject> subjects = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public ClassGrade() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getGradeNumber() { return gradeNumber; }
    public void setGradeNumber(int gradeNumber) { this.gradeNumber = gradeNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }
    public List<Subject> getSubjects() { return subjects; }
    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private int gradeNumber;
        private String name;
        private String description;
        private Board board;

        public Builder gradeNumber(int gradeNumber) { this.gradeNumber = gradeNumber; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder board(Board board) { this.board = board; return this; }

        public ClassGrade build() {
            ClassGrade cg = new ClassGrade();
            cg.gradeNumber = this.gradeNumber;
            cg.name = this.name;
            cg.description = this.description;
            cg.board = this.board;
            return cg;
        }
    }
}
