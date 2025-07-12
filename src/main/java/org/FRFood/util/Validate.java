package org.FRFood.util;

public class Validate {
    public static boolean validatePhoneNumber(String phone) {
        return (phone.length() == 11) ;
    }
    public static void validatePhone(String phone) throws DataValidationException {
        if (phone.length() != 11) {throw new DataValidationException("{\"error\":\"Invalid `phone`\"}");}
    }
    public static boolean validateEmail(String email) {return true;}
    public static void validateName(String name) throws DataValidationException {
        if (name.isEmpty()) {throw new DataValidationException("{\"error\":\"Invalid `name`\"}");}
    }
}
