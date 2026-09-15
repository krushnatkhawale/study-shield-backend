package com.studyshield.studyshield.content.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "board_class",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"board_id", "class_level_id"})
        })
public class BoardClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_level_id", nullable = false)
    private ClassLevel classLevel;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    public BoardClass() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }
    public ClassLevel getClassLevel() { return classLevel; }
    public void setClassLevel(ClassLevel classLevel) { this.classLevel = classLevel; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
