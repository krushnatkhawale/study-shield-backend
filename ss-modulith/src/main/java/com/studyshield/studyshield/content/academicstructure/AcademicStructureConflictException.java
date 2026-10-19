package com.studyshield.studyshield.content.academicstructure;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 409-style business conflict on the academic-structure endpoints: empty-before-load
 * guard, missing parents, blocked deletes. Carries a structured payload that the
 * global handler renders verbatim.
 */
public class AcademicStructureConflictException extends RuntimeException {

    private final Map<String, Object> payload;

    public AcademicStructureConflictException(String message, Map<String, Object> payload) {
        super(message);
        this.payload = new LinkedHashMap<>(payload);
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
