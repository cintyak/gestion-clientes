package bo.com.bisa.gpgw.msaccount.ms.service;

import bo.com.bisa.gpgw.msaccount.config.ApplicationProperties;
import bo.com.bisa.gpgw.msaccount.ms.exceptions.MsApiException;
import bo.com.bisa.gpgw.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PaymentMsService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String apiUrl;

    public PaymentMsService(ApplicationProperties applicationProperties) {
        this.apiUrl = applicationProperties.getMicroservices().getGatewayUrl().concat("/payment");
    }

    public void webhookTransactionRegister(String txCode) throws MsApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("txCode", txCode);
        log.debug("Request to /internal/api/notif-transaction/register, body: {}", JsonUtil.write(body));
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl.concat("/internal/api/notif-transaction/register"), body, String.class);
        log.debug("Response notif transaction: {}", response);
        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            throw new MsApiException(response);
        }
    }

    @Async
    public void webhookTransactionRegisterAsync(String txCode) {
        try {
            Thread.sleep(3000);
            webhookTransactionRegister(txCode);
        } catch (MsApiException e) {
            log.error("Failed to register transaction webhook in paymentms: {}, message: {}", txCode, e.getMessage());
        } catch (InterruptedException e) {
            log.error("Se interrumpio el proceso de espera: {}", e.getMessage());
        }
    }

       try {
            String url = apiUrl.concat("/internal/api/shopping-mods/webhooks");
            ResponseEntity<String> response = restTemplate.postForEntity(url, body, String.class);
            log.info("Notificacion exitoso para procesar en el modulo de compras: {}", response.getStatusCode());

        } catch (HttpClientErrorException e) {
            throw new MsApiException(e);
        }
    }
}
