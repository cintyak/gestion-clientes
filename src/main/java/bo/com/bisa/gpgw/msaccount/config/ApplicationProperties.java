package bo.com.bisa.gpgw.msaccount.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Getter
@Setter
public class ApplicationProperties {

   private final Account account = new Account();
    private final Microservices microservices = new Microservices();
   
    @Getter
    @Setter
    public static class Microservices {
        private String gatewayUrl;
    }
    @Setter
    @Getter
    public static class Payment {

        private String paymentBaseUrl;

    }
    @Setter
    @Getter
    public static class Account {

        private String accountBaseUrl;
        private String username;
        private String password;

    }
}
