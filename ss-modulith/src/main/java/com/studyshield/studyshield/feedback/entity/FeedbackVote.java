package com.studyshield.studyshield.feedback.entity;

/**
 * The vote a user gives a question while reviewing it. Up/down is once-per-user
 * per question and reversible (tapping again, or switching direction, updates the row).
 */
public enum FeedbackVote {
    UP, DOWN, NONE
}
