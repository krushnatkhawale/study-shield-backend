package com.studyshield.studyshield.content.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "boards")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** null for ALL (generic / board-agnostic). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(nullable = false, unique = true)
    @Size(max = 255)
    private String name;

    @Column(nullable = false, unique = true)
    @Size(max = 255)
    private String code;

    private String description;

    @Column(name = "min_ordinal", nullable = false)
    private int minOrdinal;

    /** inclusive; IT may be 17, others 16. */
    @Column(name = "max_ordinal", nullable = false)
    private int maxOrdinal;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Board() {}

    public Board(String name, String code, String description, boolean active) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getMinOrdinal() { return minOrdinal; }
    public void setMinOrdinal(int minOrdinal) { this.minOrdinal = minOrdinal; }
    public int getMaxOrdinal() { return maxOrdinal; }
    public void setMaxOrdinal(int maxOrdinal) { this.maxOrdinal = maxOrdinal; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Country country;
        private String name;
        private String code;
        private String description;
        private int minOrdinal = 1;
        private int maxOrdinal = 16;
        private boolean active = true;

        public Builder country(Country country) { this.country = country; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder minOrdinal(int minOrdinal) { this.minOrdinal = minOrdinal; return this; }
        public Builder maxOrdinal(int maxOrdinal) { this.maxOrdinal = maxOrdinal; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public Board build() {
            Board board = new Board();
            board.country = this.country;
            board.name = this.name;
            board.code = this.code;
            board.description = this.description;
            board.minOrdinal = this.minOrdinal;
            board.maxOrdinal = this.maxOrdinal;
            board.active = this.active;
            return board;
        }
    }
}