package com.giftplatform.security;

import java.nio.charset.StandardCharsets;

/** Applied only when choosing credentials; existing logins remain compatible. */
public final class CredentialPolicy {
    private CredentialPolicy() {}
    public static final String USERNAME_PATTERN = "^[A-Za-z0-9][A-Za-z0-9_]{3,29}$";
    public static final String USERNAME_MESSAGE = "Username must be 4–30 characters, contain only letters, numbers and underscores, and start with a letter or number.";
    public static void validatePassword(String value) {
        if (value == null || value.codePointCount(0, value.length()) < 8)
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        if (!value.matches("(?s).*[A-Z].*")) throw new IllegalArgumentException("Password must contain an uppercase letter.");
        if (!value.matches("(?s).*[a-z].*")) throw new IllegalArgumentException("Password must contain a lowercase letter.");
        if (!value.matches("(?s).*[0-9].*")) throw new IllegalArgumentException("Password must contain a number.");
        if (value.chars().noneMatch(c -> (c >= 33 && c <= 47) || (c >= 58 && c <= 64) || (c >= 91 && c <= 96) || (c >= 123 && c <= 126))) throw new IllegalArgumentException("Password must contain a special character.");
        if (value.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Password must not exceed 72 UTF-8 bytes.");
    }
}
