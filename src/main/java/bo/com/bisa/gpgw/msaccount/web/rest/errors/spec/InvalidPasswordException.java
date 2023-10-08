package bo.com.bisa.gpgw.msaccount.web.rest.errors.spec;

import bo.com.bisa.gpgw.msaccount.web.rest.errors.BadRequestAlertException;

public class InvalidPasswordException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public InvalidPasswordException() {
        super("INVALID_PASSWORD", "Invalid password!");
    }
}
