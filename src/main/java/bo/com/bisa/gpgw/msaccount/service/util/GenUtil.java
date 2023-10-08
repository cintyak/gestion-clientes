package bo.com.bisa.gpgw.msaccount.service.util;

import java.util.UUID;

public class GenUtil {

    public static String randomUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
