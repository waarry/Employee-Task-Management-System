package com.employee.exception;

/** Thrown when a logged-in user tries something their role does not allow. */
public class AuthorizationException extends AppException {

    public AuthorizationException(String message) {
        super(message);
    }
}