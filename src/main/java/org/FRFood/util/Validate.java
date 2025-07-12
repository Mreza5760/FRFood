package org.FRFood.util;

public class Validate {
    public static boolean validatePhoneNumber(String phone) {
        return (phone.length() == 11) ;
    }
    public static void validatePhone(String phone) throws Exception {
        if (phone.length() != 11) {throw new Exception("{\"error\":\"Invalid `field phone`\"}");}
    }
    public static boolean validateEmail(String email) {return true;}
    public static void validateName(String name) throws Exception {
        if (name.isEmpty()) {throw new Exception("{\"error\":\"Invalid `field name`\"}");}
    }
}
