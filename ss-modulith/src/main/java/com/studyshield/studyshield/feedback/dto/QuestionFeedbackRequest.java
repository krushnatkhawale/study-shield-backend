package com.studyshield.studyshield.feedback.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Feedback a user submits against one question during review.
 * <p>
 * Semantics:
 * <ul>
 *   <li>{@code vote} — one of UP, DOWN, NONE (once-per-user, reversible).</li>
 *   <li>{@code downCategory} — optional reason only meaningful when vote is DOWN:
 *       WRONG_ANSWER, TYPO, OFFENSIVE, OTHER.</li>
 *   <li>{@code comment} — optional for up/down; required when {@code report} is true.</li>
 *   <li>{@code report} — marks the question as reported (always with a comment).</li>
 * </ul>
 *
 * @param vote        the vote value as a string (UP | DOWN | NONE)
 * @param downCategory optional category attached to a DOWN vote
 * @param comment     optional free-text comment
 * @param report      whether the question is being reported
 */
public record QuestionFeedbackRequest(
    @NotBlank String vote,
    String downCategory,
    String comment,
    Boolean report
) {}
