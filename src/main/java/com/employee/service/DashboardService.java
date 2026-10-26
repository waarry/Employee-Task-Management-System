package com.employee.service;

import com.employee.dao.DashboardDao;
import com.employee.model.DashboardStats;
import com.employee.model.User;

/**
 * Business rules for the dashboards:
 *  - only a manager may see company-wide numbers
 *  - an employee sees ONLY their own numbers, and the employee id comes
 *    from the logged-in session, never from the screen, so it cannot be faked.
 */
public class DashboardService {

    private final DashboardDao dao;

    public DashboardService() {
        this(new DashboardDao());
    }

    public DashboardService(DashboardDao dao) {
        this.dao = dao;
    }

    public DashboardStats getManagerStats() {
        SessionContext.requireManager();
        return dao.getManagerStats();
    }

    public DashboardStats getMyStats() {
        User user = SessionContext.requireEmployee();
        return dao.getEmployeeStats(user.getEmployeeId());
    }
}