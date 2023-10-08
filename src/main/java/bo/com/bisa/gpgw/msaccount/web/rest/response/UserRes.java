package bo.com.bisa.gpgw.msaccount.web.rest.response;

import bo.com.bisa.gpgw.domain.Authority;
import bo.com.bisa.gpgw.domain.User;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserRes {

    private Long id;

    private String login;

    private String firstName;

    private String lastName;

    private String email;

    private Boolean activated;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Long companyServiceId;

    private Set<String> authorities;

    public UserRes(User entity) {
        this.id = entity.getId();
        this.login = entity.getLogin();
        this.firstName = entity.getFirstName();
        this.lastName = entity.getLastName();
        this.email = entity.getEmail();
        this.activated = entity.isActivated();
        this.createdBy = entity.getCreatedBy();
        this.createdDate = entity.getCreatedDate();
        this.lastModifiedBy = entity.getLastModifiedBy();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.companyServiceId = entity.getCompanyService().getId();
        if (entity.getAuthorities() != null) {
            this.authorities = entity.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet());
        }
    }
}
