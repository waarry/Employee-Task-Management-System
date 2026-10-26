package com.employee.exception;

/**
 * Thrown when something goes wrong while talking to MySQL.
 * The technical SQLException is kept as the "cause" (for the log),
 * while the message is a friendly sentence for the user.
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}