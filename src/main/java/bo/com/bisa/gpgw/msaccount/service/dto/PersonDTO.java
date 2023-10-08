package bo.com.bisa.gpgw.msaccount.service.dto;

import bo.com.bisa.gpgw.domain.enumeration.CurrencyTypeEnum;
import lombok.Data;

import javax.persistence.Lob;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TransactionDTO implements Serializable {

    private Long id;
   
    @NotNull
    @Size(max = 30)
    private String customerUserId;
   
    @NotNull
    @Size(max = 100)
    private String firstName;

    @NotNull
    @Size(max = 100)
    private String lastName;

    @NotNull
    @Size(max = 255)
    private String birthdate

    @Size(max = 255)
    private String address;

    @NotNull
    @Size(max = 100)
    private String documentId;

    

}
