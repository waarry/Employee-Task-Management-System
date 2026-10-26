package com.employee.service;

import com.employee.dao.UserDao;
import com.employee.exception.AuthenticationException;
import com.employee.exception.AuthorizationException;
import com.employee.exception.DatabaseException;
import com.employee.exception.ValidationException;
import com.employee.model.Role;
import com.employee.model.User;
import com.employee.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests login and permission checks using a FAKE UserDao (no database needed). */
class AuthServiceTest {

    private static String hash;

    private UserDao dao;
    private AuthService service;

    @BeforeAll
    static void createHash() {
        hash = PasswordUtil.hash("Secret123");   // slow, so do it once
    }

    @BeforeEach
    void setUp() {
        dao = mock(UserDao.class);
        service = new AuthService(dao);
        SessionContext.clear();
    }

    @AfterEach
    void tearDown() {
        SessionContext.clear();
    }

    private User user(String name, Role role, boolean active) {
        return new User(1, name, hash, role, active, 1, "Test Person");
    }

    @Test
    void successfulLoginReturnsUserAndStartsSession() {
        User manager = user("manager1", Role.MANAGER, true);
        when(dao.findByUsername("manager1")).thenReturn(Optional.of(manager));

        User result = service.login("manager1", "Secret123");

        assertSame(manager, result);
        assertTrue(SessionContext.isLoggedIn());
    }

    @Test
    void wrongPasswordIsRejected() {
        when(dao.findByUsername("manager1")).thenReturn(Optional.of(user("manager1", Role.MANAGER, true)));

        AuthenticationException e = assertThrows(AuthenticationException.class,
                () -> service.login("manager1", "WrongPass1"));

        assertEquals("Invalid username or password.", e.getMessage());
        assertFalse(SessionContext.isLoggedIn());
    }

    @Test
    void unknownUserGetsTheSameMessageAsWrongPassword() {
        when(dao.findByUsername("ghost")).thenReturn(Optional.empty());

        AuthenticationException e = assertThrows(AuthenticationException.class,
                () -> service.login("ghost", "Secret123"));

        assertEquals("Invalid username or password.", e.getMessage());
    }

    @Test
    void deactivatedAccountCannotLogIn() {
        when(dao.findByUsername("old")).thenReturn(Optional.of(user("old", Role.EMPLOYEE, false)));

        assertThrows(AuthenticationException.class, () -> service.login("old", "Secret123"));
        assertFalse(SessionContext.isLoggedIn());
    }

    @Test
    void blankUsernameOrPasswordIsRejected() {
        assertThrows(ValidationException.class, () -> service.login("  ", "Secret123"));
        assertThrows(ValidationException.class, () -> service.login("manager1", ""));
        assertThrows(ValidationException.class, () -> service.login(null, null));
    }

    @Test
    void databaseErrorIsPassedUpAsDatabaseException() {
        when(dao.findByUsername("manager1")).thenThrow(new DatabaseException("DB down"));

        assertThrows(DatabaseException.class, () -> service.login("manager1", "Secret123"));
    }

    @Test
    void logoutClearsTheSession() {
        when(dao.findByUsername("manager1")).thenReturn(Optional.of(user("manager1", Role.MANAGER, true)));
        service.login("manager1", "Secret123");

        service.logout();

        assertFalse(SessionContext.isLoggedIn());
    }

    @Test
    void managerPassesManagerCheckButNotEmployeeCheck() {
        SessionContext.setCurrentUser(user("manager1", Role.MANAGER, true));

        assertEquals("manager1", SessionContext.requireManager().getUsername());
        assertThrows(AuthorizationException.class, SessionContext::requireEmployee);
    }

    @Test
    void employeeCannotUseManagerFunctions() {
        SessionContext.setCurrentUser(user("emp1", Role.EMPLOYEE, true));

        assertThrows(AuthorizationException.class, SessionContext::requireManager);
        assertEquals("emp1", SessionContext.requireEmployee().getUsername());
    }

    @Test
    void nobodyLoggedInIsRejected() {
        assertThrows(AuthenticationException.class, SessionContext::requireLogin);
        assertThrows(AuthenticationException.class, SessionContext::requireManager);
    }
}