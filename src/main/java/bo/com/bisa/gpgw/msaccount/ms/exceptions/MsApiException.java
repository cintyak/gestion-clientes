package bo.com.bisa.gpgw.msaccount.ms.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

@Getter
public class MsApiException extends Exception {

    private HttpStatus status;

    private String body;

    public MsApiException(ResponseEntity<String> response) {
        this.status = response.getStatusCode();
        this.body = response.getBody();
    }

    public MsApiException(HttpClientErrorException exception) {
        this.status = exception.getStatusCode();
        this.body = exception.getResponseBodyAsString();
    }

    @Override
    public String toString() {
        return "MsApiException{" +
            "status=" + status +
            ", body='" + body + '\'' +
            "} " + super.toString();
    }
}
