package com.studyshield.tv.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id));
    }
    public ResourceNotFoundException(String resource, String externalId) {
        super(String.format("%s not found with externalId: %s", resource, externalId));
    }
}
