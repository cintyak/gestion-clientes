package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.*;
import bo.com.bisa.gpgw.msaccount.config.Constants;
import bo.com.bisa.gpgw.msaccount.repository.AuthorityRepository;
import bo.com.bisa.gpgw.msaccount.repository.UserRepository;
import bo.com.bisa.gpgw.msaccount.security.AuthoritiesConstants;
import bo.com.bisa.gpgw.msaccount.service.UserService;
import bo.com.bisa.gpgw.msaccount.service.exceptions.EmailAlreadyUsedException;
import bo.com.bisa.gpgw.msaccount.web.rest.errors.NotFoundAlertException;
import bo.com.bisa.gpgw.msaccount.web.rest.errors.spec.LoginAlreadyUsedException;
import bo.com.bisa.gpgw.msaccount.web.rest.request.UserCreateReq;
import bo.com.bisa.gpgw.msaccount.web.rest.request.UserUpdateReq;
import bo.com.bisa.gpgw.msaccount.web.rest.response.UserRes;
import bo.com.bisa.gpgw.msaccount.web.rest.util.PaginationUtil;
import bo.com.bisa.gpgw.msaccount.web.rest.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserResource {

    private final UserService userService;

    private final UserRepository userRepository;

    private final CompanyRepository companyRepository;

    private final CompanyServiceRepository companyServiceRepository;

    private final AuthorityRepository authorityRepository;

    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UserRes> createUser(@Valid @RequestBody UserCreateReq request) throws URISyntaxException {
        log.debug("REST request to save User: {}", request);
        if (userRepository.findOneByLogin(request.getLogin().toLowerCase()).isPresent()) {
            throw new LoginAlreadyUsedException();
        } else if (userRepository.findOneByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        }
        CompanyService companyService = companyServiceRepository.findById(request.getCompanyServiceId()).orElseThrow(NotFoundAlertException::new);
        User result = userService.saveFromRequest(request, companyService);
        return ResponseEntity
            .created(new URI("/api/users/" + result.getLogin()))
            .body(new UserRes(result));
    }

    @PutMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UserRes> updateUser(@Valid @RequestBody UserUpdateReq request) {
        log.debug("REST request to update User: {}", request);
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(request.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(request.getId()))) {
            throw new EmailAlreadyUsedException();
        }
        CompanyService companyService = companyServiceRepository.findById(request.getCompanyServiceId()).orElseThrow(NotFoundAlertException::new);
        Optional<UserRes> result = userService.partialUpdateFromRequest(request, companyService).map(UserRes::new);
        return ResponseUtil.wrapOrNotFound(result);
    }

    @DeleteMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        log.debug("REST request to delete User: {}", login);
        if (!userRepository.existsByLogin(login)) throw new NotFoundAlertException();
        userService.deleteUser(login);
        return ResponseEntity
            .noContent()
            .build();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) Long id) {
        log.debug("REST request to delete User: {}", id);
        if (!userRepository.existsById(id)) throw new NotFoundAlertException();
        userService.deleteUser(id);
        return ResponseEntity
            .noContent()
            .build();
    }

    @GetMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UserRes> getUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        log.debug("REST request to get User: {}", login);
        Optional<UserRes> result = userRepository.findOneWithAuthoritiesByLogin(login).map(UserRes::new);
        return ResponseUtil.wrapOrNotFound(result);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<UserRes> getUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) Long id) {
        log.debug("REST request to get User: {}", id);
        Optional<UserRes> result = userRepository.findOneWithAuthoritiesById(id).map(UserRes::new);
        return ResponseUtil.wrapOrNotFound(result);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<UserRes>> getAllUsers(Pageable pageable) {
        log.debug("REST request to get all User for an admin");
        final Page<UserRes> page = userRepository.findAll(pageable).map(UserRes::new);
        HttpHeaders headers = PaginationUtil.addTotalCountHttpHeaders(page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/users/companyService/{companyServiceId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<UserRes>> getAllUsers(Pageable pageable,@PathVariable Long companyServiceId) {
        log.debug("REST request to get all User for an admin");
        final Page<UserRes> page = userRepository.findAllByCompanyService(pageable,companyServiceId).map(UserRes::new);
        HttpHeaders headers = PaginationUtil.addTotalCountHttpHeaders(page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/users/authorities")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<Authority>> getAllAuhtorities(Pageable pageable) {
        Page<Authority> page = authorityRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.addTotalCountHttpHeaders(page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

}
