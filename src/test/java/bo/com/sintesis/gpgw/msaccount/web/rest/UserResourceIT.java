package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Company;
import bo.com.bisa.gpgw.domain.User;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.UserRepository;
import bo.com.bisa.gpgw.msaccount.security.AuthoritiesConstants;
import bo.com.bisa.gpgw.msaccount.web.rest.request.UserCreateReq;
import bo.com.bisa.gpgw.msaccount.web.rest.request.UserUpdateReq;
import bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@IntegrationTest
class UserResourceIT {

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String UPDATED_LOGIN = "jhipster";

    private static final Long DEFAULT_ID = 1L;

    private static final String DEFAULT_PASSWORD = "passjohndoe";
    private static final String UPDATED_PASSWORD = "passjhipster";

    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String UPDATED_EMAIL = "jhipster@localhost";

    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String UPDATED_FIRSTNAME = "jhipsterFirstName";

    private static final String DEFAULT_LASTNAME = "doe";
    private static final String UPDATED_LASTNAME = "jhipsterLastName";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserMockMvc;

    private User user;

    private UserCreateReq userCreateReq;

    public static User createEntity(EntityManager em) {
        User user = User.builder()
            .login(DEFAULT_LOGIN)
            .password(DEFAULT_PASSWORD)
            .email(DEFAULT_EMAIL)
            .firstName(DEFAULT_FIRSTNAME)
            .lastName(DEFAULT_LASTNAME)
            .activated(true)
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

    public static UserCreateReq createRequest(User user) {
        return UserCreateReq.builder()
            .login(user.getLogin())
            .password(user.getPassword())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
//            .companyId(user.getCompany().getId())
            .authorities(Collections.singleton(AuthoritiesConstants.USER))
            .build();
    }

    @BeforeEach
    public void initTest() {
        user = createEntity(em);
        userCreateReq = createRequest(user);
    }

    @Test
    @Transactional
    void createUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        restUserMockMvc
            .perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userCreateReq)))
            .andExpect(status().isCreated());

        // Validate the User in the database
        assertPersistedUsers(
            users -> {
                assertThat(users).hasSize(databaseSizeBeforeCreate + 1);
                User testUser = users.get(users.size() - 1);
                assertThat(testUser.getLogin()).isEqualTo(DEFAULT_LOGIN);
                assertThat(testUser.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
                assertThat(testUser.getLastName()).isEqualTo(DEFAULT_LASTNAME);
                assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
            }
        );
    }

    @Test
    @Transactional
    void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        UserCreateReq userCreateReq = createRequest(user);
        userCreateReq.setLogin(DEFAULT_LOGIN);

        // Create the User
        restUserMockMvc
            .perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userCreateReq)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    @Transactional
    void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        UserCreateReq userCreateReq = createRequest(user);
        userCreateReq.setEmail(DEFAULT_EMAIL);

        // Create the User
        restUserMockMvc
            .perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userCreateReq)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    @Transactional
    void updateUser() throws Exception {
        // Initialize the database
        User updatedUser = userRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        UserUpdateReq userUpdateReq = UserUpdateReq.builder()
            .id(updatedUser.getId())
            .firstName(UPDATED_FIRSTNAME)
            .lastName(UPDATED_LASTNAME)
            .email(UPDATED_EMAIL)
            .activated(updatedUser.isActivated())
//            .companyId(updatedUser.getCompany().getId())
            .authorities(Collections.singleton(AuthoritiesConstants.USER))
            .build();

        restUserMockMvc
            .perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userUpdateReq)))
            .andExpect(status().isOk());

        // Validate the User in the database
        assertPersistedUsers(
            users -> {
                assertThat(users).hasSize(databaseSizeBeforeUpdate);
                User testUser = users.stream().filter(usr -> usr.getId().equals(updatedUser.getId())).findFirst().get();
                assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
                assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
                assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
            }
        );
    }

    @Test
    @Transactional
    void deleteUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeDelete = userRepository.findAll().size();

        // Delete the user
        restUserMockMvc
            .perform(delete("/api/users/{login}", user.getLogin())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeDelete - 1));
    }

    @Test
    @Transactional
    void getUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get the user
        restUserMockMvc
            .perform(get("/api/users/{login}", user.getLogin()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(user.getLogin()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL));
    }

    @Test
    @Transactional
    void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown")).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getAllUsers() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get all the users
        restUserMockMvc
            .perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));
    }

    private void assertPersistedUsers(Consumer<List<User>> userAssertion) {
        userAssertion.accept(userRepository.findAll());
    }
}
