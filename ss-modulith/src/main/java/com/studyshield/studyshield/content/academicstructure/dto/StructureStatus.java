package com.studyshield.studyshield.content.academicstructure.dto;

import java.util.List;

/**
 * Response of {@code GET /api/v1/academic-structure/status}: current row counts,
 * the configured pinned ref, and where the load source points.
 */
public record StructureStatus(
        String source,
        String repo,
        String ref,
        List<EntityCount> entities
) {
    public record EntityCount(
            String entity,
            long rowCount
    ) {}
}
