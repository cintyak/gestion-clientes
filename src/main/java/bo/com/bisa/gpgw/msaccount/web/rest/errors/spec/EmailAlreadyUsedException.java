package bo.com.bisa.gpgw.msaccount.web.rest.errors.spec;

import bo.com.bisa.gpgw.msaccount.web.rest.errors.BadRequestAlertException;

public class EmailAlreadyUsedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public EmailAlreadyUsedException() {
        super("EMAIL_EXISTS", "Email is already in use!");
    }
}
