package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.*;
import bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.UserRepository;
import bo.com.bisa.gpgw.msaccount.web.rest.request.LoginReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@IntegrationTest
class UserJWTControllerIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    private User user;

    public static User createEntity(EntityManager em, PasswordEncoder passwordEncoder) {
        User user = User.builder()
            .login("user-jwt-controller")
            .email("user-jwt-controller@example.com")
            .activated(true)
            .password(passwordEncoder.encode("test"))
            .build();

        // Add required entity
        Company company;
        List<Company> companies = TestUtil.findAll(em, Company.class);
        if (companies.isEmpty()) {
            company = Company.builder()
                .name("Company name test")
                .businessName("Company businessName test")
                .city("Company city test")
                .phone("Company phone test")
                .address("Company address test")
                .notificationEmail("Company notificationEmail test")
                .active(true)
                .build();
            em.persist(company);
            em.flush();
        } else {
            company = companies.get(0);
        }
//        user.setCompany(company);

        return user;
    }

    @BeforeEach
    public void initTest() {
        user = createEntity(em, passwordEncoder);
    }

    @Test
    @Transactional
    void testAuthorize() throws Exception {
        user.setLogin("user-jwt-controller");
        user.setEmail("user-jwt-controller@example.com");
        user.setPassword(passwordEncoder.encode("test"));

        userRepository.saveAndFlush(user);
        System.out.println("Saved: " + user);

        LoginReq login = new LoginReq();
        login.setUsername("user-jwt-controller");
        login.setPassword("test");
        mockMvc
            .perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id_token").isString())
            .andExpect(jsonPath("$.id_token").isNotEmpty())
            .andExpect(header().string("Authorization", not(nullValue())))
            .andExpect(header().string("Authorization", not(is(emptyString()))));
    }

    @Test
    @Transactional
    void testAuthorizeWithRememberMe() throws Exception {
        user.setLogin("user-jwt-controller-remember-me");
        user.setEmail("user-jwt-controller-remember-me@example.com");
        user.setActivated(true);
        user.setPassword(passwordEncoder.encode("test"));

        userRepository.saveAndFlush(user);
        System.out.println("Saved: " + user);

        LoginReq login = new LoginReq();
        login.setUsername("user-jwt-controller-remember-me");
        login.setPassword("test");
        login.setRememberMe(true);
        mockMvc
            .perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id_token").isString())
            .andExpect(jsonPath("$.id_token").isNotEmpty())
            .andExpect(header().string("Authorization", not(nullValue())))
            .andExpect(header().string("Authorization", not(is(emptyString()))));
    }

    @Test
    void testAuthorizeFails() throws Exception {
        LoginReq login = new LoginReq();
        login.setUsername("wrong-user");
        login.setPassword("wrong password");
        mockMvc
            .perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(login)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.id_token").doesNotExist())
            .andExpect(header().doesNotExist("Authorization"));
    }
}
