package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Card;
import bo.com.bisa.gpgw.domain.DebitHeader;
import bo.com.bisa.gpgw.domain.enumeration.DebitHeaderStatusEnum;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.service.DebitHeaderService;
import bo.com.bisa.gpgw.msaccount.service.dto.DebitHeaderDTO;
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
import java.math.BigDecimal;
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
class DebitHeaderResourceIT {

    private static final String DEFAULT_COMERCE_SERVICE_ID = "AAAAAAAAAA";
    private static final String UPDATED_COMERCE_SERVICE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_NETWORK_TRANSACTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_NETWORK_TRANSACTION_ID = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_ORIGINAL_AMOUNT = new BigDecimal("0.20");
    private static final BigDecimal UPDATED_ORIGINAL_AMOUNT = new BigDecimal(1);

    private static final String DEFAULT_CYBER_TRANSACTION_ID = "AAAAAAAAAA";
    private static final String UPDATED_CYBER_TRANSACTION_ID = "BBBBBBBBBB";

    private static final DebitHeaderStatusEnum DEFAULT_STATUS = DebitHeaderStatusEnum.ACTIVE;
    private static final DebitHeaderStatusEnum UPDATED_STATUS = DebitHeaderStatusEnum.UNCOLLECTIVE;

    private static final String ENTITY_API_URL = "/api/debit-headers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private DebitHeaderRepository debitHeaderRepository;

    @Autowired
    private DebitHeaderService debitHeaderService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDebitHeaderMockMvc;

    private DebitHeader debitHeader;

    public static DebitHeader createEntity(EntityManager em) {
        DebitHeader debitHeader = DebitHeader.builder()
            .commerceServiceId(DEFAULT_COMERCE_SERVICE_ID)
            .networkTransactionId(DEFAULT_NETWORK_TRANSACTION_ID)
            .originalAmount(DEFAULT_ORIGINAL_AMOUNT)
            .cyberTransactionId(DEFAULT_CYBER_TRANSACTION_ID)
            .status(DEFAULT_STATUS)
            .build();
        // Add required entity
        Card card;
        if (TestUtil.findAll(em, Card.class).isEmpty()) {
            card = CardResourceIT.createEntity(em);
            em.persist(card);
            em.flush();
        } else {
            card = TestUtil.findAll(em, Card.class).get(0);
        }
        debitHeader.setCard(card);
        return debitHeader;
    }

    @BeforeEach
    public void initTest() {
        debitHeader = createEntity(em);
    }

    @Test
    @Transactional
    void createDebitHeader() throws Exception {
        int databaseSizeBeforeCreate = debitHeaderRepository.findAll().size();
        // Create the DebitHeader
        DebitHeaderDTO debitHeaderDTO = debitHeaderService.toDto(debitHeader);
        restDebitHeaderMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(debitHeaderDTO))
            )
            .andExpect(status().isCreated());

        // Validate the DebitHeader in the database
        List<DebitHeader> debitHeaderList = debitHeaderRepository.findAll();
        assertThat(debitHeaderList).hasSize(databaseSizeBeforeCreate + 1);
        DebitHeader testDebitHeader = debitHeaderList.get(debitHeaderList.size() - 1);
        assertThat(testDebitHeader.getCommerceServiceId()).isEqualTo(DEFAULT_COMERCE_SERVICE_ID);
        assertThat(testDebitHeader.getNetworkTransactionId()).isEqualTo(DEFAULT_NETWORK_TRANSACTION_ID);
        assertThat(testDebitHeader.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void checkNetworkTransactionIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = debitHeaderRepository.findAll().size();
        // set the field null
        debitHeader.setNetworkTransactionId(null);

        // Create the DebitHeader, which fails.
        DebitHeaderDTO debitHeaderDTO = debitHeaderService.toDto(debitHeader);

        restDebitHeaderMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(debitHeaderDTO))
            )
            .andExpect(status().isBadRequest());

        List<DebitHeader> debitHeaderList = debitHeaderRepository.findAll();
        assertThat(debitHeaderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDebitHeaders() throws Exception {
        // Initialize the database
        debitHeaderRepository.saveAndFlush(debitHeader);

        // Get all the debitHeaderList
        restDebitHeaderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(debitHeader.getId().intValue())))
            .andExpect(jsonPath("$.[*].comerceServiceId").value(hasItem(DEFAULT_COMERCE_SERVICE_ID)))
            .andExpect(jsonPath("$.[*].networkTransactionId").value(hasItem(DEFAULT_NETWORK_TRANSACTION_ID)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    void getDebitHeader() throws Exception {
        // Initialize the database
        debitHeaderRepository.saveAndFlush(debitHeader);

        // Get the debitHeader
        restDebitHeaderMockMvc
            .perform(get(ENTITY_API_URL_ID, debitHeader.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(debitHeader.getId().intValue()))
            .andExpect(jsonPath("$.comerceServiceId").value(DEFAULT_COMERCE_SERVICE_ID))
            .andExpect(jsonPath("$.networkTransactionId").value(DEFAULT_NETWORK_TRANSACTION_ID))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingDebitHeader() throws Exception {
        // Get the debitHeader
        restDebitHeaderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewDebitHeader() throws Exception {
        // Initialize the database
        DebitHeader updatedDebitHeader = debitHeaderRepository.saveAndFlush(debitHeader);

        int databaseSizeBeforeUpdate = debitHeaderRepository.findAll().size();

        updatedDebitHeader = DebitHeader.builder()
            .id(updatedDebitHeader.getId())
            .commerceServiceId(UPDATED_COMERCE_SERVICE_ID)
            .networkTransactionId(UPDATED_NETWORK_TRANSACTION_ID)
            .originalAmount(UPDATED_ORIGINAL_AMOUNT)
            .cyberTransactionId(UPDATED_CYBER_TRANSACTION_ID)
            .status(UPDATED_STATUS)
            .card(updatedDebitHeader.getCard())
            .build();
        DebitHeaderDTO debitHeaderDTO = debitHeaderService.toDto(updatedDebitHeader);

        restDebitHeaderMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(debitHeaderDTO))
            )
            .andExpect(status().isOk());

        // Validate the DebitHeader in the database
        List<DebitHeader> debitHeaderList = debitHeaderRepository.findAll();
        assertThat(debitHeaderList).hasSize(databaseSizeBeforeUpdate);
        DebitHeader testDebitHeader = debitHeaderList.get(debitHeaderList.size() - 1);
        assertThat(testDebitHeader.getCommerceServiceId()).isEqualTo(UPDATED_COMERCE_SERVICE_ID);
        assertThat(testDebitHeader.getNetworkTransactionId()).isEqualTo(UPDATED_NETWORK_TRANSACTION_ID);
        assertThat(testDebitHeader.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void putNonExistingDebitHeader() throws Exception {
        int databaseSizeBeforeUpdate = debitHeaderRepository.findAll().size();
        debitHeader.setId(count.incrementAndGet());

        // Create the DebitHeader
        DebitHeaderDTO debitHeaderDTO = debitHeaderService.toDto(debitHeader);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDebitHeaderMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(debitHeaderDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the DebitHeader in the database
        List<DebitHeader> debitHeaderList = debitHeaderRepository.findAll();
        assertThat(debitHeaderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDebitHeader() throws Exception {
        // Initialize the database
        debitHeaderRepository.saveAndFlush(debitHeader);

        int databaseSizeBeforeDelete = debitHeaderRepository.findAll().size();

        // Delete the debitHeader
        restDebitHeaderMockMvc
            .perform(delete(ENTITY_API_URL_ID, debitHeader.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<DebitHeader> debitHeaderList = debitHeaderRepository.findAll();
        assertThat(debitHeaderList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
