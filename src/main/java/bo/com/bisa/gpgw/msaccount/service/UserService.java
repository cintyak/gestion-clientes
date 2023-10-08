package bo.com.bisa.gpgw.msaccount.service;

import bo.com.bisa.gpgw.domain.Authority;
import bo.com.bisa.gpgw.domain.Company;
import bo.com.bisa.gpgw.domain.CompanyService;
import bo.com.bisa.gpgw.domain.User;
import bo.com.bisa.gpgw.msaccount.repository.AuthorityRepository;
import bo.com.bisa.gpgw.msaccount.repository.UserRepository;
import bo.com.bisa.gpgw.msaccount.security.SecurityUtils;
import bo.com.bisa.gpgw.msaccount.service.exceptions.AuthorityNotFoundException;
import bo.com.bisa.gpgw.msaccount.service.exceptions.EmailAlreadyUsedException;
import bo.com.bisa.gpgw.msaccount.web.rest.errors.NotFoundAlertException;
import bo.com.bisa.gpgw.msaccount.web.rest.request.UserCreateReq;
import bo.com.bisa.gpgw.msaccount.web.rest.request.UserUpdateReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    @Transactional
    public User saveFromRequest(UserCreateReq request, CompanyService companyService) {
        User entity = User.builder()
            .login(request.getLogin())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .email(request.getEmail() != null ? request.getEmail().toLowerCase() : null)
            .lastName(request.getLastName())
            .activated(true)
            .companyService(companyService)
            .build();

        if (request.getAuthorities() != null) {
            Iterator it=request.getAuthorities().iterator();
            while(it.hasNext()) {
                String role = it.next().toString();
                List<Authority> listAuthority = authorityRepository.findByName(role);
                log.info("listAuthority.." + listAuthority.size());
                if (listAuthority.isEmpty()) {
                    throw new AuthorityNotFoundException();

                } entity.setAuthorities(request.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet()));
            }
        }

        entity = userRepository.save(entity);
        log.info("User created: {}", entity);
        return entity;
    }

    @Transactional
    public Optional<User> partialUpdateFromRequest(UserUpdateReq request, CompanyService companyService) {
        return userRepository
            .findById(request.getId())
            .map(existingUser -> {
                if (request.getFirstName() != null) {
                    existingUser.setFirstName(request.getFirstName());
                }
                if (request.getLastName() != null) {
                    existingUser.setLastName(request.getLastName());
                }
                if (request.getEmail() != null) {
                    existingUser.setEmail(request.getEmail());
                }
                if (request.getActivated() != null) {
                    existingUser.setActivated(request.getActivated());
                }
                if (request.getCompanyServiceId() != null && companyService != null) {
                    existingUser.setCompanyService(companyService);
                }
                if (request.getAuthorities() != null && !request.getAuthorities().isEmpty()) {
                        Iterator it=request.getAuthorities().iterator();
                        while(it.hasNext()) {
                            String role = it.next().toString();
                            List<Authority> listAuthority = authorityRepository.findByName(role);
                            if (listAuthority.isEmpty()) {
                                throw new AuthorityNotFoundException();
                            }
                            existingUser.setAuthorities(request.getAuthorities().stream()
                                .map(authorityRepository::findById)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toSet()));
                        }
                }

                log.info("User updated: {}", existingUser);
                return existingUser;
            });
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login)
            .ifPresent(user -> {
                    userRepository.delete(user);
                    log.info("User deleted: {}", user);
                }
            );
    }

    public void deleteUser(Long id) {
        userRepository.findOneById(id)
            .ifPresent(user -> {
                    userRepository.delete(user);
                    log.info("User deleted: {}", user);
                }
            );
    }

    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }
}
