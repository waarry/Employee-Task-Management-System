package com.employee.model;

/**
 * The numbers shown on a dashboard.
 * A "record" is a short way to write a class that only holds data:
 * Java creates the constructor and the getters (e.g. stats.pending()) for us.
 */
public record DashboardStats(int totalEmployees,
                             int totalTasks,
                             int pendingTasks,
                             int inProgressTasks,
                             int completedTasks,
                             int overdueTasks) {
}