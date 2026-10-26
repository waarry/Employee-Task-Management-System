package com.employee.dao;

import com.employee.config.DatabaseConnection;
import com.employee.exception.DatabaseException;
import com.employee.model.DashboardStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SQL for the dashboard numbers. Every number is counted by MySQL.
 *
 * "Overdue" is calculated, never stored: due date before today AND the task
 * is still PENDING or IN_PROGRESS. A COMPLETED or CANCELLED task is never overdue.
 */
public class DashboardDao {

    private static final Logger LOGGER = Logger.getLogger(DashboardDao.class.getName());

    // COALESCE turns NULL into 0 when the tasks table is empty.
    private static final String TASK_COUNTS =
            "COUNT(*) AS total_tasks, "
                    + "COALESCE(SUM(status = 'PENDING'), 0) AS pending_tasks, "
                    + "COALESCE(SUM(status = 'IN_PROGRESS'), 0) AS in_progress_tasks, "
                    + "COALESCE(SUM(status = 'COMPLETED'), 0) AS completed_tasks, "
                    + "COALESCE(SUM(status IN ('PENDING','IN_PROGRESS') AND due_date < CURDATE()), 0) "
                    + "AS overdue_tasks ";

    /** Statistics for ALL tasks plus the number of active employees. */
    public DashboardStats getManagerStats() {
        String sql = "SELECT (SELECT COUNT(*) FROM employees e JOIN users u ON u.user_id = e.user_id "
                + "WHERE u.role = 'EMPLOYEE' AND e.is_active = 1) AS total_employees, "
                + TASK_COUNTS
                + "FROM tasks";
        return query(sql, null);
    }

    /** Statistics for the tasks of ONE employee only. */
    public DashboardStats getEmployeeStats(int employeeId) {
        String sql = "SELECT 0 AS total_employees, " + TASK_COUNTS
                + "FROM tasks WHERE assigned_to = ?";
        return query(sql, employeeId);
    }

    private DashboardStats query(String sql, Integer employeeId) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (employeeId != null) {
                ps.setInt(1, employeeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new DashboardStats(
                        rs.getInt("total_employees"),
                        rs.getInt("total_tasks"),
                        rs.getInt("pending_tasks"),
                        rs.getInt("in_progress_tasks"),
                        rs.getInt("completed_tasks"),
                        rs.getInt("overdue_tasks"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Dashboard query failed", e);
            throw new DatabaseException("Could not load the dashboard numbers. Please try again.", e);
        }
    }
}