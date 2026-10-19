package com.studyshield.studyshield.content.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * The offering: the combined reference a board + class level + subject. Content packs
 * and quizzes anchor here — never to a display name. One row per (board_class, subject).
 */
@Entity
@Table(name = "board_class_subject", uniqueConstraints = {
        @UniqueConstraint(name = "uk_board_class_subject_offering", columnNames = {"board_class_id", "subject_id"})
})
public class BoardClassSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_class_id", nullable = false)
    private BoardClass boardClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public BoardClassSubject() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BoardClass getBoardClass() { return boardClass; }
    public void setBoardClass(BoardClass boardClass) { this.boardClass = boardClass; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}