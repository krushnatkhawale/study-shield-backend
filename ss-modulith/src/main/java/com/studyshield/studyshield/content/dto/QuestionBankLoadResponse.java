package com.studyshield.studyshield.content.dto;

/**
 * Response for {@code POST /api/v1/questions/load}.
 */
public record QuestionBankLoadResponse(
        int boardsCreated,
        int boardClassesCreated,
        int offeringsCreated,
        int subjectsCreated,
        int contentPacksCreated,
        int quizzesCreated,
        int questionsCreated,
        int questionsSkipped
) {}