package bo.com.bisa.gpgw.msaccount.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NotFoundEntityException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public NotFoundEntityException (String entity, Long id) {
        super(String.format("The entity %s not found with id: %s", entity, id));
    }
}
