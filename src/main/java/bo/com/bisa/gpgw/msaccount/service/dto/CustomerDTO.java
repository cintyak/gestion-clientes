package bo.com.bisa.gpgw.msaccount.service.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class CustomerDTO implements Serializable {

    private Long id;

   @NotNull
    @Size(max = 100)
    private String age

    @NotNull
    @Size(max = 100)
    private String postalCode;

    @NotNull
    @Size(max = 100)
    private String country;

    @NotNull
    @Size(max = 100)
    private String email;

    @NotNull
    @Size(max = 30)
    private String phoneNumber;

    @NotNull
    @Size(max = 255)
    private String state;

}
