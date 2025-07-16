package org.FRFood.util;

public class Validation {
    static public boolean validatePhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.length() == 11 && phoneNumber.startsWith("0");
    }
}
