package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Customer;
import bo.com.bisa.gpgw.domain.PgwMid;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.CustomerRepository;
import bo.com.bisa.gpgw.msaccount.service.CustomerService;
import bo.com.bisa.gpgw.msaccount.service.PgwMidService;
import bo.com.bisa.gpgw.msaccount.service.dto.CustomerDTO;
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
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CustomerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CustomerResourceIT {

    private static final String DEFAULT_CUSTOMER_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_CUSTOMER_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_FIRST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAAAAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS_1 = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS_1 = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS_2 = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS_2 = "BBBBBBBBBB";

    private static final String DEFAULT_LOCALITY = "AAAAAAAAAA";
    private static final String UPDATED_LOCALITY = "BBBBBBBBBB";

    private static final String DEFAULT_ADMINISTRATIVE_AREA = "AAAAAAAAAA";
    private static final String UPDATED_ADMINISTRATIVE_AREA = "BBBBBBBBBB";

    private static final String DEFAULT_POSTAL_CODE = "AAAAAAAAAA";
    private static final String UPDATED_POSTAL_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_COUNTRY = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_CELL_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_CELL_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/customers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PgwMidService pgwMidService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCustomerMockMvc;

    private Customer customer;

    public Customer createEntity(EntityManager em) {
        Long paymentGatewayId = 1L;
        Customer customer = new Customer();
//        customer.setCustomerUserId(DEFAULT_CUSTOMER_USER_ID);
//        customer.setFirstName(DEFAULT_FIRST_NAME);
//        customer.setLastName(DEFAULT_LAST_NAME);
//        customer.setAddress1(DEFAULT_ADDRESS_1);
//        customer.setAddress2(DEFAULT_ADDRESS_2);
//        customer.setLocality(DEFAULT_LOCALITY);
//        customer.setAdministrativeArea(DEFAULT_ADMINISTRATIVE_AREA);
//        customer.setPostalCode(DEFAULT_POSTAL_CODE);
//        customer.setCountry(DEFAULT_COUNTRY);
//        customer.setEmail(DEFAULT_EMAIL);
//        customer.setPhoneNumber(DEFAULT_CELL_PHONE);
//        customer.setCustomerTokenId(DEFAULT_TOKEN);
        // Add required entity
        Optional<PgwMid> midOptional = pgwMidService.getByPaymentGatewayId(paymentGatewayId);
//        customer.setPgwMid(midOptional.get());
        return customer;
    }

    @BeforeEach
    public void initTest() {
        customer = createEntity(em);
    }

    @Test
    @Transactional
    void createCustomer() throws Exception {
        int databaseSizeBeforeCreate = customerRepository.findAll().size();
        // Create the Customer
        CustomerDTO customerDTO = customerService.toDto(customer);
        restCustomerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isCreated());

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeCreate + 1);
        Customer testCustomer = customerList.get(customerList.size() - 1);
//        assertThat(testCustomer.getCustomerUserId()).isEqualTo(DEFAULT_CUSTOMER_USER_ID);
//        assertThat(testCustomer.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
//        assertThat(testCustomer.getLastName()).isEqualTo(DEFAULT_LAST_NAME);
//        assertThat(testCustomer.getEmail()).isEqualTo(DEFAULT_EMAIL);
//        assertThat(testCustomer.getPhoneNumber()).isEqualTo(DEFAULT_CELL_PHONE);
//        assertThat(testCustomer.getCustomerTokenId()).isEqualTo(DEFAULT_TOKEN);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
//        customer.setEmail(null);

        // Create the Customer, which fails.
        CustomerDTO customerDTO = customerService.toDto(customer);

        restCustomerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTokenIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().size();
        // set the field null
//        customer.setCustomerTokenId(null);

        // Create the Customer, which fails.
        CustomerDTO customerDTO = customerService.toDto(customer);

        restCustomerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(customerDTO)))
            .andExpect(status().isBadRequest());

        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCustomers() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get all the customerList
        restCustomerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(customer.getId().intValue())))
            .andExpect(jsonPath("$.[*].customerUserId").value(hasItem(DEFAULT_CUSTOMER_USER_ID)))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].phoneNumber").value(hasItem(DEFAULT_CELL_PHONE)))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)));
    }

    @Test
    @Transactional
    void getCustomer() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        // Get the customer
        restCustomerMockMvc
            .perform(get(ENTITY_API_URL_ID, customer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(customer.getId().intValue()))
            .andExpect(jsonPath("$.customerUserId").value(DEFAULT_CUSTOMER_USER_ID))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.phoneNumber").value(DEFAULT_CELL_PHONE))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN));
    }

    @Test
    @Transactional
    void getNonExistingCustomer() throws Exception {
        // Get the customer
        restCustomerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }


    @Test
    @Transactional
    void putNewCustomer() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        int databaseSizeBeforeUpdate = customerRepository.findAll().size();

        // Update the customer
        Customer updatedCustomer = customerRepository.findById(customer.getId()).orElse(null);
        assertThat(updatedCustomer).isNotNull();
        // Disconnect from session so that the updates on updatedCustomer are not directly saved in db
        //em.detach(updatedCustomer);
//        updatedCustomer.setCustomerUserId(UPDATED_CUSTOMER_USER_ID);
//        updatedCustomer.setFirstName(UPDATED_FIRST_NAME);
//        updatedCustomer.setLastName(UPDATED_LAST_NAME);
//        updatedCustomer.setAddress1(UPDATED_ADDRESS_1);
//        updatedCustomer.setAddress2(UPDATED_ADDRESS_2);
//        updatedCustomer.setLocality(UPDATED_LOCALITY);
//        updatedCustomer.setAdministrativeArea(UPDATED_ADMINISTRATIVE_AREA);
//        updatedCustomer.setPostalCode(UPDATED_POSTAL_CODE);
//        updatedCustomer.setCountry(UPDATED_COUNTRY);
//        updatedCustomer.setEmail(UPDATED_EMAIL);
//        updatedCustomer.setPhoneNumber(UPDATED_CELL_PHONE);
//        updatedCustomer.setCustomerTokenId(UPDATED_TOKEN);
        CustomerDTO customerDTO = customerService.toDto(updatedCustomer);

        restCustomerMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(customerDTO))
            )
            .andExpect(status().isOk());

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
        Customer testCustomer = customerList.get(customerList.size() - 1);
//        assertThat(testCustomer.getCustomerUserId()).isEqualTo(UPDATED_CUSTOMER_USER_ID);
//        assertThat(testCustomer.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
//        assertThat(testCustomer.getLastName()).isEqualTo(UPDATED_LAST_NAME);
//        assertThat(testCustomer.getEmail()).isEqualTo(UPDATED_EMAIL);
//        assertThat(testCustomer.getPhoneNumber()).isEqualTo(UPDATED_CELL_PHONE);
//        assertThat(testCustomer.getCustomerTokenId()).isEqualTo(UPDATED_TOKEN);
    }

    @Test
    @Transactional
    void putNonExistingCustomer() throws Exception {
        int databaseSizeBeforeUpdate = customerRepository.findAll().size();
        customer.setId(count.incrementAndGet());

        // Create the Customer
        CustomerDTO customerDTO = customerService.toDto(customer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCustomerMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(customerDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCustomer() throws Exception {
        // Initialize the database
        customerRepository.saveAndFlush(customer);

        int databaseSizeBeforeDelete = customerRepository.findAll().size();

        // Delete the customer
        restCustomerMockMvc
            .perform(delete(ENTITY_API_URL_ID, customer.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Customer> customerList = customerRepository.findAll();
        assertThat(customerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
