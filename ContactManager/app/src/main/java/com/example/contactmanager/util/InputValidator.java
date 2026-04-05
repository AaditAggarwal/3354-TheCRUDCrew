package com.example.contactmanager.util;

/**
 * InputValidator: Validates user-supplied data before it reaches the database.
 * Satisfies NFR: "User input shall be validated to prevent invalid or malicious data."
 */
public class InputValidator {

    /** A valid name is non-null, non-blank, and at most 100 characters. */
    public static boolean isValidName(String name) {
        return name != null
            && !name.trim().isEmpty()
            && name.trim().length() <= 100;
    }

    /**
     * A valid phone number consists of an optional leading '+', then 7–15 digits.
     * Spaces, dashes, and parentheses are stripped before the check.
     */
    public static boolean isValidPhoneNumber(String number) {
        if (number == null || number.trim().isEmpty()) return false;
        String cleaned = number.replaceAll("[\\s\\-().]+", "");
        return cleaned.matches("^\\+?[0-9]{7,15}$");
    }

    /** A valid group name is non-null, non-blank, and at most 50 characters. */
    public static boolean isValidGroupName(String name) {
        return name != null
            && !name.trim().isEmpty()
            && name.trim().length() <= 50;
    }
}
