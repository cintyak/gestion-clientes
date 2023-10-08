package bo.com.bisa.gpgw.msaccount.service.exceptions;

public class EntityNotFoundException extends RuntimeException{
    public EntityNotFoundException(String entity, Long id) {
        super(String.format("Not found entity: %s with id:", entity, id));
    }
}
