package bo.com.bisa.gpgw.msaccount.web.rest.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
public class UserUpdateReq {

    private Long id;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    @NotNull
    private Boolean activated;

    @NotNull
    private Long companyServiceId;

    private Set<String> authorities;
}
