package bo.com.bisa.gpgw.msaccount.service.exceptions;

public class AuthorityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthorityNotFoundException() {
        super("Role does not exist!");
    }
}
