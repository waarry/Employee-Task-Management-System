package com.employee.exception;

/** Thrown when the user's input is missing or invalid. */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message);
    }
}