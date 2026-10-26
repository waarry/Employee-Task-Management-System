package com.employee.service;

import com.employee.dao.UserDao;
import com.employee.exception.AuthenticationException;
import com.employee.exception.ValidationException;
import com.employee.model.User;
import com.employee.util.PasswordUtil;

import java.util.Optional;

/**
 * Business logic for logging in and out.
 * The UI will call login(); it never talks to the database directly.
 */
public class AuthService {

    // One message for "no such user" AND "wrong password", so an attacker
    // cannot discover which usernames exist.
    private static final String INVALID_LOGIN = "Invalid username or password.";

    private final UserDao userDao;

    public AuthService() {
        this(new UserDao());
    }

    /** This constructor lets tests give in a fake UserDao. */
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Checks the credentials. On success the user is stored in SessionContext
     * and returned, so the UI can open the correct dashboard.
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Please enter your username.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Please enter your password.");
        }

        Optional<User> found = userDao.findByUsername(username.trim());
        if (found.isEmpty() || !PasswordUtil.matches(password, found.get().getPasswordHash())) {
            throw new AuthenticationException(INVALID_LOGIN);
        }

        User user = found.get();
        if (!user.isActive()) {
            throw new AuthenticationException("This account has been deactivated. Please contact your manager.");
        }

        SessionContext.setCurrentUser(user);
        return user;
    }

    public void logout() {
        SessionContext.clear();
    }
}