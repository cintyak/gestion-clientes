package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.msaccount.service.UserService;
import bo.com.bisa.gpgw.msaccount.web.rest.response.UserRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final UserService userService;

    @GetMapping("/account")
    public UserRes getAccount() {
        return userService
            .getUserWithAuthorities()
            .map(UserRes::new)
            .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }
}
