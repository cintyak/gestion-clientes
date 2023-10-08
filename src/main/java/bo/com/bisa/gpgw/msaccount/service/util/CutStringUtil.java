package bo.com.bisa.gpgw.msaccount.service.util;

public class CutStringUtil {

    public static String cut1000(String value) {
        if (value == null) return null;
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
