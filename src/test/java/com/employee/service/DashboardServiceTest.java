package com.employee.service;

import com.employee.dao.DashboardDao;
import com.employee.exception.AuthenticationException;
import com.employee.exception.AuthorizationException;
import com.employee.model.DashboardStats;
import com.employee.model.Role;
import com.employee.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests the dashboard permission rules with a FAKE DashboardDao (no database needed). */
class DashboardServiceTest {

    private DashboardDao dao;
    private DashboardService service;
    private final DashboardStats stats = new DashboardStats(3, 10, 4, 3, 2, 1);

    @BeforeEach
    void setUp() {
        dao = mock(DashboardDao.class);
        service = new DashboardService(dao);
        SessionContext.clear();
    }

    @AfterEach
    void tearDown() {
        SessionContext.clear();
    }

    private User user(Role role, int employeeId) {
        return new User(1, "someone", "hash", role, true, employeeId, "Some One");
    }

    @Test
    void managerGetsCompanyStats() {
        SessionContext.setCurrentUser(user(Role.MANAGER, 1));
        when(dao.getManagerStats()).thenReturn(stats);

        assertSame(stats, service.getManagerStats());
    }

    @Test
    void employeeCannotReadManagerStats() {
        SessionContext.setCurrentUser(user(Role.EMPLOYEE, 7));

        assertThrows(AuthorizationException.class, () -> service.getManagerStats());
        verify(dao, never()).getManagerStats();
    }

    @Test
    void managerCannotUseEmployeeStats() {
        SessionContext.setCurrentUser(user(Role.MANAGER, 1));

        assertThrows(AuthorizationException.class, () -> service.getMyStats());
    }

    @Test
    void employeeSeesOnlyTheirOwnStats() {
        SessionContext.setCurrentUser(user(Role.EMPLOYEE, 7));
        when(dao.getEmployeeStats(7)).thenReturn(stats);

        assertSame(stats, service.getMyStats());
        verify(dao).getEmployeeStats(7);
        verify(dao, never()).getManagerStats();
    }

    @Test
    void nobodyLoggedInIsRejected() {
        assertThrows(AuthenticationException.class, () -> service.getManagerStats());
        assertThrows(AuthenticationException.class, () -> service.getMyStats());
    }
}