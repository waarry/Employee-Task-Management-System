package com.employee.service;

import com.employee.exception.AuthenticationException;
import com.employee.exception.AuthorizationException;
import com.employee.model.Role;
import com.employee.model.User;

/**
 * Remembers WHO is logged in while the program runs, and offers the
 * permission checks used by every service in the project.
 *
 * Because services call requireManager() / requireEmployee() themselves,
 * permissions are enforced in the backend, not just by hiding buttons.
 */
public final class SessionContext {

    private static User currentUser;

    private SessionContext() {
    }

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Called on logout. */
    public static synchronized void clear() {
        currentUser = null;
    }

    /** Returns the logged-in user, or throws if nobody is logged in. */
    public static User requireLogin() {
        User user = getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("Please log in first.");
        }
        return user;
    }

    /** Returns the logged-in user only if they have the given role. */
    public static User requireRole(Role role) {
        User user = requireLogin();
        if (user.getRole() != role) {
            throw new AuthorizationException("You do not have permission to perform this action.");
        }
        return user;
    }

    public static User requireManager() {
        return requireRole(Role.MANAGER);
    }

    public static User requireEmployee() {
        return requireRole(Role.EMPLOYEE);
    }
}