package bo.com.bisa.gpgw.msaccount.web.rest.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
public class HeadingReq {

    private Long id;

    @NotNull
    @Size(max = 30)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    private Boolean active;

    @NotNull
    private Set<Long> mddIds;
}
