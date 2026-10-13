package com.file.demo.dto;

import lombok.Data;

@Data
public class ValidationResult {
    private boolean valid;
    private String message;
    private String correctedValue;

    public ValidationResult(boolean valid, String message, String correctedValue) {
        this.valid = valid;
        this.message = message;
        this.correctedValue = correctedValue;
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult of(boolean valid, String message) {
        return new ValidationResult(valid, message, null);
    }

    public static ValidationResult of(boolean valid, String message, String correctedValue) {
        return new ValidationResult(valid, message, correctedValue);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public String getCorrectedValue() {
        return correctedValue;
    }

    public boolean hasCorrectedValue() {
        return correctedValue != null && !correctedValue.isEmpty();
    }
}
