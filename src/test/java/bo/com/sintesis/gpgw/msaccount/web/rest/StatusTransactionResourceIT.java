package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.StatusTransaction;
import bo.com.bisa.gpgw.domain.Transaction;
import bo.com.bisa.gpgw.domain.enumeration.StatusTransactionEnum;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.StatusTransactionRepository;
import bo.com.bisa.gpgw.msaccount.service.StatusTransactionService;
import bo.com.bisa.gpgw.msaccount.service.dto.StatusTransactionDTO;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class StatusTransactionResourceIT {

    private static final StatusTransactionEnum DEFAULT_STATUS = StatusTransactionEnum.STARTED;
    private static final StatusTransactionEnum UPDATED_STATUS = StatusTransactionEnum.PROCESSED;

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/status-transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private StatusTransactionRepository statusTransactionRepository;

    @Autowired
    private StatusTransactionService statusTransactionService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restStatusTransactionMockMvc;

    private StatusTransaction statusTransaction;

    public static StatusTransaction createEntity(EntityManager em) {
        StatusTransaction statusTransaction = StatusTransaction.builder()
            .status(DEFAULT_STATUS)
            .description(DEFAULT_DESCRIPTION)
            .createdDate(DEFAULT_CREATED_DATE)
            .active(DEFAULT_ACTIVE)
            .build();
        // Add required entity
        Transaction transaction;
        if (TestUtil.findAll(em, Transaction.class).isEmpty()) {
            transaction = TransactionResourceIT.createEntity(em);
            em.persist(transaction);
            em.flush();
        } else {
            transaction = TestUtil.findAll(em, Transaction.class).get(0);
        }
        statusTransaction.setTransaction(transaction);
        return statusTransaction;
    }

    @BeforeEach
    public void initTest() {
        statusTransaction = createEntity(em);
    }

    @Test
    @Transactional
    void createStatusTransaction() throws Exception {
        int databaseSizeBeforeCreate = statusTransactionRepository.findAll().size();
        // Create the StatusTransaction
        StatusTransactionDTO statusTransactionDTO = statusTransactionService.toDto(statusTransaction);
        restStatusTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(statusTransactionDTO))
            )
            .andExpect(status().isCreated());

        // Validate the StatusTransaction in the database
        List<StatusTransaction> statusTransactionList = statusTransactionRepository.findAll();
        assertThat(statusTransactionList).hasSize(databaseSizeBeforeCreate + 1);
        StatusTransaction testStatusTransaction = statusTransactionList.get(statusTransactionList.size() - 1);
        assertThat(testStatusTransaction.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testStatusTransaction.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testStatusTransaction.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testStatusTransaction.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = statusTransactionRepository.findAll().size();
        // set the field null
        statusTransaction.setStatus(null);

        // Create the StatusTransaction, which fails.
        StatusTransactionDTO statusTransactionDTO = statusTransactionService.toDto(statusTransaction);

        restStatusTransactionMockMvc
            .perform(
                post(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(statusTransactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<StatusTransaction> statusTransactionList = statusTransactionRepository.findAll();
        assertThat(statusTransactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllStatusTransactions() throws Exception {
        // Initialize the database
        statusTransactionRepository.saveAndFlush(statusTransaction);

        // Get all the statusTransactionList
        restStatusTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(statusTransaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getStatusTransaction() throws Exception {
        // Initialize the database
        statusTransactionRepository.saveAndFlush(statusTransaction);

        // Get the statusTransaction
        restStatusTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, statusTransaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(statusTransaction.getId().intValue()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingStatusTransaction() throws Exception {
        // Get the statusTransaction
        restStatusTransactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewStatusTransaction() throws Exception {
        // Initialize the database
        StatusTransaction updatedStatusTransaction = statusTransactionRepository.saveAndFlush(statusTransaction);

        int databaseSizeBeforeUpdate = statusTransactionRepository.findAll().size();

        updatedStatusTransaction = StatusTransaction.builder()
            .id(updatedStatusTransaction.getId())
            .transaction(updatedStatusTransaction.getTransaction())
            .status(UPDATED_STATUS)
            .description(UPDATED_DESCRIPTION)
            .createdDate(UPDATED_CREATED_DATE)
            .active(UPDATED_ACTIVE)
            .build();
        StatusTransactionDTO statusTransactionDTO = statusTransactionService.toDto(updatedStatusTransaction);

        restStatusTransactionMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(statusTransactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the StatusTransaction in the database
        List<StatusTransaction> statusTransactionList = statusTransactionRepository.findAll();
        assertThat(statusTransactionList).hasSize(databaseSizeBeforeUpdate);
        StatusTransaction testStatusTransaction = statusTransactionList.get(statusTransactionList.size() - 1);
        assertThat(testStatusTransaction.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testStatusTransaction.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testStatusTransaction.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testStatusTransaction.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingStatusTransaction() throws Exception {
        int databaseSizeBeforeUpdate = statusTransactionRepository.findAll().size();
        statusTransaction.setId(count.incrementAndGet());

        // Create the StatusTransaction
        StatusTransactionDTO statusTransactionDTO = statusTransactionService.toDto(statusTransaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStatusTransactionMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(statusTransactionDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the StatusTransaction in the database
        List<StatusTransaction> statusTransactionList = statusTransactionRepository.findAll();
        assertThat(statusTransactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteStatusTransaction() throws Exception {
        // Initialize the database
        statusTransactionRepository.saveAndFlush(statusTransaction);

        int databaseSizeBeforeDelete = statusTransactionRepository.findAll().size();

        // Delete the statusTransaction
        restStatusTransactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, statusTransaction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<StatusTransaction> statusTransactionList = statusTransactionRepository.findAll();
        assertThat(statusTransactionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
