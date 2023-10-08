package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Card;
import bo.com.bisa.gpgw.domain.DebitDetails;
import bo.com.bisa.gpgw.domain.DebitHeader;
import bo.com.bisa.gpgw.domain.enumeration.DebitDetailsStatusEnum;
import bo.com.bisa.gpgw.domain.enumeration.DebitHeaderStatusEnum;
import bo.com.bisa.gpgw.domain.enumeration.CurrencyTypeEnum;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.service.DebitDetailsService;
import bo.com.bisa.gpgw.msaccount.service.dto.DebitDetailsDTO;
import bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class DebitDetailsResourceIT {

    private static final LocalDate DEFAULT_EXECUTION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EXECUTION_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_CUSTOMER_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_CUSTOMER_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_GLOSS = "AAAAAAAAAA";
    private static final String UPDATED_GLOSS = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("0.20");
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(1);

    private static final CurrencyTypeEnum DEFAULT_CURRENCY_TYPE = CurrencyTypeEnum.BOB;
    private static final CurrencyTypeEnum UPDATED_CURRENCY_TYPE = CurrencyTypeEnum.USD;

    private static final BigDecimal DEFAULT_CHANGE_TYPE = new BigDecimal(1);
    private static final BigDecimal UPDATED_CHANGE_TYPE = new BigDecimal(2);

    private static final Integer DEFAULT_FAILED_ATTEMP = 1;
    private static final Integer UPDATED_FAILED_ATTEMP = 2;

    private static final Boolean DEFAULT_LOCKED = false;
    private static final Boolean UPDATED_LOCKED = true;

    private static final DebitDetailsStatusEnum DEFAULT_STATUS = DebitDetailsStatusEnum.PENDING;
    private static final DebitDetailsStatusEnum UPDATED_STATUS = DebitDetailsStatusEnum.PROCESSED;

    private static final String ENTITY_API_URL = "/api/debit-details";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private DebitDetailsRepository debitDetailsRepository;

    @Autowired
    private DebitDetailsService debitDetailsService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDebitDetailsMockMvc;

    private DebitDetails debitDetails;

    public static DebitDetails createEntity(EntityManager em) {
        DebitDetails debitDetails = DebitDetails.builder()
            .executionDate(DEFAULT_EXECUTION_DATE)
            .customerUserId(DEFAULT_CUSTOMER_USER_ID)
            .gloss(DEFAULT_GLOSS)
            .amount(DEFAULT_AMOUNT)
            .currencyType(DEFAULT_CURRENCY_TYPE)
            .changeType(DEFAULT_CHANGE_TYPE)
            .failedAttemp(DEFAULT_FAILED_ATTEMP)
            .locked(DEFAULT_LOCKED)
            .status(DEFAULT_STATUS)
            .build();
        // Add required entity
        DebitHeader debitHeader;
        Card card;
        List<Card> cards = TestUtil.findAll(em, Card.class);
        if (cards.isEmpty()) {
            card = CardResourceIT.createEntity(em);
            em.persist(card);
            em.flush();
        }
        card = TestUtil.findAll(em, Card.class).get(0);
        if (TestUtil.findAll(em, DebitHeader.class).isEmpty()) {
            debitHeader = DebitHeader.builder()
                .status(DebitHeaderStatusEnum.ACTIVE)
                .card(card)
                .commerceServiceId("commerceId")
                .networkTransactionId("network")
                .build();
            //debitHeader = DebitHeaderResourceIT.createEntity(em);
            em.persist(debitHeader);
            em.flush();
        } else {
            debitHeader = TestUtil.findAll(em, DebitHeader.class).get(0);
        }
        debitDetails.setDebitHeader(debitHeader);
        return debitDetails;
    }

    @BeforeEach
    public void initTest() {
        debitDetails = createEntity(em);
    }

    @Test
    @Transactional
    void createDebitDetails() throws Exception {
        int databaseSizeBeforeCreate = debitDetailsRepository.findAll().size();
        // Create the DebitDetails
        DebitDetailsDTO debitDetailsDTO = debitDetailsService.toDto(debitDetails);
        restDebitDetailsMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(debitDetailsDTO))
            )
            .andExpect(status().isCreated());

        // Validate the DebitDetails in the database
        List<DebitDetails> debitDetailsList = debitDetailsRepository.findAll();
        assertThat(debitDetailsList).hasSize(databaseSizeBeforeCreate + 1);
        DebitDetails testDebitDetails = debitDetailsList.get(debitDetailsList.size() - 1);
        assertThat(testDebitDetails.getExecutionDate()).isEqualTo(DEFAULT_EXECUTION_DATE);
        assertThat(testDebitDetails.getCustomerUserId()).isEqualTo(DEFAULT_CUSTOMER_USER_ID);
        assertThat(testDebitDetails.getGloss()).isEqualTo(DEFAULT_GLOSS);
        assertThat(testDebitDetails.getAmount()).isEqualByComparingTo(DEFAULT_AMOUNT);
        assertThat(testDebitDetails.getCurrencyType()).isEqualTo(DEFAULT_CURRENCY_TYPE);
        assertThat(testDebitDetails.getChangeType()).isEqualByComparingTo(DEFAULT_CHANGE_TYPE);
        assertThat(testDebitDetails.getFailedAttemp()).isEqualTo(DEFAULT_FAILED_ATTEMP);
        assertThat(testDebitDetails.getLocked()).isEqualTo(DEFAULT_LOCKED);
        assertThat(testDebitDetails.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void checkCustomerUserIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = debitDetailsRepository.findAll().size();
        // set the field null
        debitDetails.setCustomerUserId(null);

        // Create the DebitDetails, which fails.
        DebitDetailsDTO debitDetailsDTO = debitDetailsService.toDto(debitDetails);

        restDebitDetailsMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(debitDetailsDTO))
            )
            .andExpect(status().isBadRequest());

        List<DebitDetails> debitDetailsList = debitDetailsRepository.findAll();
        assertThat(debitDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDebitDetails() throws Exception {
        // Initialize the database
        debitDetailsRepository.saveAndFlush(debitDetails);

        // Get all the debitDetailsList
        restDebitDetailsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(debitDetails.getId().intValue())))
            .andExpect(jsonPath("$.[*].executionDate").value(hasItem(DEFAULT_EXECUTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].customerUserId").value(hasItem(DEFAULT_CUSTOMER_USER_ID)))
            .andExpect(jsonPath("$.[*].gloss").value(hasItem(DEFAULT_GLOSS)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currencyType").value(hasItem(DEFAULT_CURRENCY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].changeType").value(hasItem(sameNumber(DEFAULT_CHANGE_TYPE))))
            .andExpect(jsonPath("$.[*].failedAttemp").value(hasItem(DEFAULT_FAILED_ATTEMP)))
            .andExpect(jsonPath("$.[*].locked").value(hasItem(DEFAULT_LOCKED)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    void getDebitDetails() throws Exception {
        // Initialize the database
        debitDetailsRepository.saveAndFlush(debitDetails);

        // Get the debitDetails
        restDebitDetailsMockMvc
            .perform(get(ENTITY_API_URL_ID, debitDetails.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(debitDetails.getId().intValue()))
            .andExpect(jsonPath("$.executionDate").value(DEFAULT_EXECUTION_DATE.toString()))
            .andExpect(jsonPath("$.customerUserId").value(DEFAULT_CUSTOMER_USER_ID))
            .andExpect(jsonPath("$.gloss").value(DEFAULT_GLOSS))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.currencyType").value(DEFAULT_CURRENCY_TYPE.toString()))
            .andExpect(jsonPath("$.changeType").value(sameNumber(DEFAULT_CHANGE_TYPE)))
            .andExpect(jsonPath("$.failedAttemp").value(DEFAULT_FAILED_ATTEMP))
            .andExpect(jsonPath("$.locked").value(DEFAULT_LOCKED))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getNonExistingDebitDetails() throws Exception {
        // Get the debitDetails
        restDebitDetailsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewDebitDetails() throws Exception {
        // Initialize the database
        DebitDetails updatedDebitDetails = debitDetailsRepository.saveAndFlush(debitDetails);

        int databaseSizeBeforeUpdate = debitDetailsRepository.findAll().size();

        updatedDebitDetails = DebitDetails.builder()
            .id(updatedDebitDetails.getId())
            .debitHeader(updatedDebitDetails.getDebitHeader())
            .executionDate(UPDATED_EXECUTION_DATE)
            .customerUserId(UPDATED_CUSTOMER_USER_ID)
            .gloss(UPDATED_GLOSS)
            .amount(UPDATED_AMOUNT)
            .currencyType(UPDATED_CURRENCY_TYPE)
            .changeType(UPDATED_CHANGE_TYPE)
            .failedAttemp(UPDATED_FAILED_ATTEMP)
            .locked(UPDATED_LOCKED)
            .status(UPDATED_STATUS)
            .build();
        DebitDetailsDTO debitDetailsDTO = debitDetailsService.toDto(updatedDebitDetails);

        restDebitDetailsMockMvc
            .perform(
                put(ENTITY_API_URL, debitDetailsDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(debitDetailsDTO))
            )
            .andExpect(status().isOk());

        // Validate the DebitDetails in the database
        List<DebitDetails> debitDetailsList = debitDetailsRepository.findAll();
        assertThat(debitDetailsList).hasSize(databaseSizeBeforeUpdate);
        DebitDetails testDebitDetails = debitDetailsList.get(debitDetailsList.size() - 1);
        assertThat(testDebitDetails.getExecutionDate()).isEqualTo(UPDATED_EXECUTION_DATE);
        assertThat(testDebitDetails.getCustomerUserId()).isEqualTo(UPDATED_CUSTOMER_USER_ID);
        assertThat(testDebitDetails.getGloss()).isEqualTo(UPDATED_GLOSS);
        assertThat(testDebitDetails.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testDebitDetails.getCurrencyType()).isEqualTo(UPDATED_CURRENCY_TYPE);
        assertThat(testDebitDetails.getChangeType()).isEqualTo(UPDATED_CHANGE_TYPE);
        assertThat(testDebitDetails.getFailedAttemp()).isEqualTo(UPDATED_FAILED_ATTEMP);
        assertThat(testDebitDetails.getLocked()).isEqualTo(UPDATED_LOCKED);
        assertThat(testDebitDetails.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    void putNonExistingDebitDetails() throws Exception {
        int databaseSizeBeforeUpdate = debitDetailsRepository.findAll().size();
        debitDetails.setId(count.incrementAndGet());

        // Create the DebitDetails
        DebitDetailsDTO debitDetailsDTO = debitDetailsService.toDto(debitDetails);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDebitDetailsMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(debitDetailsDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the DebitDetails in the database
        List<DebitDetails> debitDetailsList = debitDetailsRepository.findAll();
        assertThat(debitDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDebitDetails() throws Exception {
        // Initialize the database
        debitDetailsRepository.saveAndFlush(debitDetails);

        int databaseSizeBeforeDelete = debitDetailsRepository.findAll().size();

        // Delete the debitDetails
        restDebitDetailsMockMvc
            .perform(delete(ENTITY_API_URL_ID, debitDetails.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<DebitDetails> debitDetailsList = debitDetailsRepository.findAll();
        assertThat(debitDetailsList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
