package bo.com.bisa.gpgw.msaccount.web.rest.errors.spec;

import bo.com.bisa.gpgw.msaccount.web.rest.errors.BadRequestAlertException;

public class AutomaticDebitAlreadyRegisteredException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public AutomaticDebitAlreadyRegisteredException(String commerceServiceId) {
        super("AUTOMATIC_DEBIT_ALREADY_REGISTERED",
            "Tarjeta registrada para debitos automaticos",
            "Esta tarjeta ya esta registrada para debitos automaticos con el commerceServicioId: " + commerceServiceId
        );
    }
}
