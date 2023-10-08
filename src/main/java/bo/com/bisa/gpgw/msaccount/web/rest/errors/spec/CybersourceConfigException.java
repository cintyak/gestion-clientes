package bo.com.bisa.gpgw.msaccount.web.rest.errors.spec;

import bo.com.bisa.gpgw.msaccount.web.rest.errors.BadRequestAlertException;

public class CybersourceConfigException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public CybersourceConfigException(String detail) {
        super("CYBERSOURCE_CONFIG_ERROR", "Cybersource error en la configuracion", detail);
    }
}
