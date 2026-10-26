package com.employee.exception;

/** Thrown when login fails, or when someone is not logged in. */
public class AuthenticationException extends AppException {

    public AuthenticationException(String message) {
        super(message);
    }
}