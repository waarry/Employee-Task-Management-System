package com.employee.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashIsNotThePlainPassword() {
        String hash = PasswordUtil.hash("Secret123");
        assertNotEquals("Secret123", hash);
        assertEquals(60, hash.length());
    }

    @Test
    void samePasswordGivesDifferentHashesBecauseOfSalt() {
        assertNotEquals(PasswordUtil.hash("Secret123"), PasswordUtil.hash("Secret123"));
    }

    @Test
    void correctPasswordMatches() {
        String hash = PasswordUtil.hash("Secret123");
        assertTrue(PasswordUtil.matches("Secret123", hash));
    }

    @Test
    void wrongPasswordDoesNotMatch() {
        String hash = PasswordUtil.hash("Secret123");
        assertFalse(PasswordUtil.matches("secret123", hash));
    }

    @Test
    void nullsAndInvalidHashesAreRejectedSafely() {
        assertFalse(PasswordUtil.matches(null, "x"));
        assertFalse(PasswordUtil.matches("Secret123", null));
        assertFalse(PasswordUtil.matches("Secret123", "not-a-real-hash"));
    }

    @Test
    void emptyPasswordCannotBeHashed() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(""));
    }
}