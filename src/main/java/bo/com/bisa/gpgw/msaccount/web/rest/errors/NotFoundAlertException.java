package bo.com.bisa.gpgw.msaccount.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class NotFoundAlertException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public NotFoundAlertException() {
        super(null, null, Status.NOT_FOUND);
    }
}
