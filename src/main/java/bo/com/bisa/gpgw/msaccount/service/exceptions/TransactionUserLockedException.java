package bo.com.bisa.gpgw.msaccount.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TransactionUserLockedException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public TransactionUserLockedException (String message) {
        super(String.format("The user is locked %s", message));
    }
}
