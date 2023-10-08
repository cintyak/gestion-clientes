package bo.com.bisa.gpgw.msaccount.web.rest.errors.spec;

import bo.com.bisa.gpgw.msaccount.web.rest.errors.BadRequestAlertException;

public class CybersourceApiException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public CybersourceApiException(String detail) {
        super("CYBERSOURCE_API_ERROR", "Cybersource error en la api de integration", detail);
    }
}
