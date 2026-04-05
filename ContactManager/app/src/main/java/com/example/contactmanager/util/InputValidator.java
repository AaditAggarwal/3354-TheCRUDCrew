package com.example.contactmanager.util;

public class InputValidator {

    public static boolean isValidName(String name) {
        return name != null
            && !name.trim().isEmpty()
            && name.trim().length() <= 100;
    }

    public static boolean isValidPhoneNumber(String number) {
        if (number == null || number.trim().isEmpty()) return false;
        String cleaned = number.replaceAll("[\\s\\-().]+", "");
        return cleaned.matches("^\\+?[0-9]{7,15}$");
    }

    public static boolean isValidGroupName(String name) {
        return name != null
            && !name.trim().isEmpty()
            && name.trim().length() <= 50;
    }
}
