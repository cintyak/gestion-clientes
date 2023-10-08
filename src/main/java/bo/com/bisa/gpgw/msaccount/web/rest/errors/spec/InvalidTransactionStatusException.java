package bo.com.bisa.gpgw.msaccount.web.rest.errors.spec;

import bo.com.bisa.gpgw.msaccount.web.rest.errors.BadRequestAlertException;

public class InvalidTransactionStatusException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public InvalidTransactionStatusException(String detail) {
        super("INVALID_TRANSACTION_STATUS", "Estado de la transaccion invalida", detail);
    }
}
