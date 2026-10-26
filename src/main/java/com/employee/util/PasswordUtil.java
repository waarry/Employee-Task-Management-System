package com.employee.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Creates and checks BCrypt password hashes.
 *
 * BCrypt adds a random "salt" to every hash, so the same password gives a
 * different hash each time. It is also deliberately slow, which makes
 * guessing passwords by brute force very expensive.
 */
public final class PasswordUtil {

    /** Work factor. Each +1 doubles the time. 12 is a good value for a desktop app. */
    private static final int COST = 12;

    private PasswordUtil() {
    }

    /** Returns a 60-character BCrypt hash to store in users.password_hash. */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty.");
        }
        return BCrypt.withDefaults().hashToString(COST, plainPassword.toCharArray());
    }

    /** Returns true only if the plain password matches the stored hash. */
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.verifyer().verify(plainPassword.toCharArray(), storedHash).verified;
        } catch (IllegalArgumentException e) {
            return false; // the stored value was not a valid hash
        }
    }
}