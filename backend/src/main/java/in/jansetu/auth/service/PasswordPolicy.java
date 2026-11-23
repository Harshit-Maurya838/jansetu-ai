package in.jansetu.auth.service;

import java.nio.charset.StandardCharsets;

/** Min 8 chars, at least one letter and one digit, max 72 bytes (BCrypt ignores the rest). */
public final class PasswordPolicy {
    private PasswordPolicy() {
    }

    public static boolean isValid(String password) {
        return password != null
                && password.length() >= 8
                && password.getBytes(StandardCharsets.UTF_8).length <= 72
                && password.chars().anyMatch(Character::isLetter)
                && password.chars().anyMatch(Character::isDigit);
    }

    public static void check(String password) {
        if (!isValid(password)) throw AuthErrors.weakPassword();
    }
}
