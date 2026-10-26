package com.employee.model;

/**
 * A login account together with its profile name.
 * Objects of this class are created by UserDao from one database row.
 */
public class User {

    private final int userId;
    private final String username;
    private final String passwordHash;
    private final Role role;
    private final boolean active;
    private final int employeeId;   // 0 if the account has no profile row
    private final String fullName;

    public User(int userId, String username, String passwordHash, Role role,
                boolean active, int employeeId, String fullName) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.employeeId = employeeId;
        this.fullName = fullName;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    /** Leaves out the password hash so it never reaches a log by accident. */
    @Override
    public String toString() {
        return "User{id=" + userId + ", username=" + username + ", role=" + role + "}";
    }
}