package cs656.com.firebasemessengerapp.utils;

public class EmailEncoding {

    public static String commaEncodePeriod(String email) {
        return email.replace(".", ",");
    }

    public static String commaDecodePeriod(String email) {
        return email.replace(",", ".");
    }
}
