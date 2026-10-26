package com.employee.exception;

/**
 * Base class for all errors we throw on purpose in this application.
 * It extends RuntimeException (an "unchecked" exception), so methods do not
 * need a "throws" clause everywhere. The message is written so that it is
 * safe to show to the user.
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}