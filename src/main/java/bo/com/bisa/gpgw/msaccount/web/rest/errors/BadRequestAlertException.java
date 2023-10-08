package bo.com.bisa.gpgw.msaccount.web.rest.errors;

import lombok.Getter;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

@Getter
public class BadRequestAlertException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    private final String errorKey;

    public BadRequestAlertException(String errorKey, String title) {
        super(null, title, Status.BAD_REQUEST, null);
        this.errorKey = errorKey;
    }

    public BadRequestAlertException(String errorKey, String title, String detail) {
        super(null, title, Status.BAD_REQUEST, detail);
        this.errorKey = errorKey;
    }
}
