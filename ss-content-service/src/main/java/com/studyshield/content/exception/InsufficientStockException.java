package com.studyshield.content.exception;

public class InsufficientStockException extends RuntimeException {

    private final int available;
    private final int required;

    public InsufficientStockException(String message, int available, int required) {
        super(message);
        this.available = available;
        this.required = required;
    }

    public int getAvailable() {
        return available;
    }

    public int getRequired() {
        return required;
    }
}
