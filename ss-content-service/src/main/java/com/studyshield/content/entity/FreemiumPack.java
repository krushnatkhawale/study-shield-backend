package com.studyshield.content.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Issued freemium pack for a device/child + class (idempotent download unit for mobile).
 */
@Entity
@Table(name = "freemium_packs", indexes = {
        @Index(name = "idx_freemium_packs_key", columnList = "idempotency_key", unique = true)
})
public class FreemiumPack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique key: className|language|boardCode|holder (childId or deviceId). */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "class_name", nullable = false, length = 64)
    private String className;

    @Column(nullable = false, length = 64)
    private String language = "English";

    @Column(name = "board_code", length = 64)
    private String boardCode = "all";

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "child_id")
    private Long childId;

    @Column(name = "user_id")
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quiz_ids", nullable = false, columnDefinition = "json")
    private List<Long> quizIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private List<String> subjects = new ArrayList<>();

    @Column(name = "quiz_count", nullable = false)
    private int quizCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FreemiumPack() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getBoardCode() { return boardCode; }
    public void setBoardCode(String boardCode) { this.boardCode = boardCode; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<Long> getQuizIds() { return quizIds; }
    public void setQuizIds(List<Long> quizIds) { this.quizIds = quizIds != null ? quizIds : new ArrayList<>(); }
    public List<String> getSubjects() { return subjects; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects != null ? subjects : new ArrayList<>(); }
    public int getQuizCount() { return quizCount; }
    public void setQuizCount(int quizCount) { this.quizCount = quizCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
