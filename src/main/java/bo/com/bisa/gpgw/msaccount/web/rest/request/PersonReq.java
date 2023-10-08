package bo.com.bisa.gpgw.msaccount.web.rest.request;

import lombok.Data;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class PersonReq {

    @Size(max = 100)
    @NotNull
    private String name;
    @Size(max = 100)
    @NotNull
    private String lastname;
    @NotNull
    private String birthdate;
    private String address
    private String documentId;

}
