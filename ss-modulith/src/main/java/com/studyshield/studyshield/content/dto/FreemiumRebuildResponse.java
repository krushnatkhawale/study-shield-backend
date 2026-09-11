package com.studyshield.studyshield.content.dto;

import java.util.List;

public record FreemiumRebuildResponse(
        long issuedBundlesDeleted,
        int classesSeeded,
        List<String> classNames
) {}
