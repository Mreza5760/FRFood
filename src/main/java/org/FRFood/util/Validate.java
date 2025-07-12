package org.FRFood.util;

public class Validate {
    public static boolean validatePhoneNumber(String phone) {
        return (phone.length() == 11) ;
    }
    public static boolean validatePhone(String phone) {
        return (!phone.isEmpty()) ;
    }
    public static boolean validateEmail(String email) {return true;}
    public static boolean validateName(String name){
        return (!name.isEmpty()) ;
    }
}
