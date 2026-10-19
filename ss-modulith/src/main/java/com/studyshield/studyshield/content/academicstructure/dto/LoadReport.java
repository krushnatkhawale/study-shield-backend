package com.studyshield.studyshield.content.academicstructure.dto;

import java.util.List;

/**
 * Response of {@code POST /api/v1/academic-structure/load/{entity}}.
 */
public record LoadReport(
        String status,
        boolean dryRun,
        String source,
        List<LoadEntityResult> entities
) {
    public record LoadEntityResult(
            String entity,
            String source,
            int rowsPlanned,
            int rowsCreated,
            int rowsSkipped,
            long ms
    ) {}
}
