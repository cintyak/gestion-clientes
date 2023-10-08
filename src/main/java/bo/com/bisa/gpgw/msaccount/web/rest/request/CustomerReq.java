package bo.com.bisa.gpgw.msaccount.web.rest.request;

import bo.com.bisa.gpgw.domain.enumeration.StatusTransactionEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.Email;

@Data
@Builder
public class CustomerReq {

   
    @Email
    private String email;
    @Size(max = 30)
    private String phone;
    @NotNull
    private String ocupation;
    @NotNull
    private String age;
    @NotNull
    private String state;
    

}
