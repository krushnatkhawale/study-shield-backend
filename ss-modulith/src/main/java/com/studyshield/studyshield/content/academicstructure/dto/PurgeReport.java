package com.studyshield.studyshield.content.academicstructure.dto;

import java.util.List;

/**
 * Response of {@code DELETE /api/v1/academic-structure/{entity}}.
 */
public record PurgeReport(
        String status,
        List<PurgeEntityResult> entities
) {
    public record PurgeEntityResult(
            String table,
            long rowsDeleted
    ) {}
}
