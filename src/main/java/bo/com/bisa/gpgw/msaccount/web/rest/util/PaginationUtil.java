package bo.com.bisa.gpgw.msaccount.web.rest.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;

public final class PaginationUtil {

    private static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";

    public static <T> HttpHeaders addTotalCountHttpHeaders(Page<T> page) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_X_TOTAL_COUNT, Long.toString(page.getTotalElements()));
        return headers;
    }
}
