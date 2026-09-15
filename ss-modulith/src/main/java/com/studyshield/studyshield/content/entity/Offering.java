package com.studyshield.studyshield.content.entity;

import jakarta.persistence.*;

/** Offering = board_class_subject row: a subject offered for a board class. */
@Entity
@Table(name = "board_class_subject",
        uniqueConstraints = @UniqueConstraint(columnNames = {"board_class_id", "subject_id"}))
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_class_id", nullable = false)
    private BoardClass boardClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    public Offering() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BoardClass getBoardClass() { return boardClass; }
    public void setBoardClass(BoardClass boardClass) { this.boardClass = boardClass; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
}
