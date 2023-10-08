package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Company;
import bo.com.bisa.gpgw.domain.PaymentGateway;
import bo.com.bisa.gpgw.domain.Transaction;
import bo.com.bisa.gpgw.domain.enumeration.CurrencyTypeEnum;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.TransactionRepository;
import bo.com.bisa.gpgw.msaccount.service.TransactionService;
import bo.com.bisa.gpgw.msaccount.service.dto.TransactionDTO;
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

import static bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TransactionResourceIT {

    private static final String DEFAULT_TX_CODE = "AAAAAAAAAA";
    private static final String UPDATED_TX_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_PROVIDER_TX_CODE = "AAAAAAAAAA";
    private static final String UPDATED_PROVIDER_TX_CODE = "BBBBBBBBBB";

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

    private static final String DEFAULT_ADDITIONAL_INFORMATION = "AAAAAAAAAA";
    private static final String UPDATED_ADDITIONAL_INFORMATION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/transactions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTransactionMockMvc;

    private Transaction transaction;

    public static Transaction createEntity(EntityManager em) {
        Transaction transaction = Transaction.builder()
            .txCode(DEFAULT_TX_CODE)
            .providerTxCode(DEFAULT_PROVIDER_TX_CODE)
//            .customerUserId(DEFAULT_CUSTOMER_USER_ID)
//            .gloss(DEFAULT_GLOSS)
            .amount(DEFAULT_AMOUNT)
            .currencyType(DEFAULT_CURRENCY_TYPE)
            .changeType(DEFAULT_CHANGE_TYPE)
            .additionalInformation(DEFAULT_ADDITIONAL_INFORMATION)
            .build();
        // Add required entity
        Company company;
        if (TestUtil.findAll(em, Company.class).isEmpty()) {
            company = CompanyResourceIT.createEntity();
            em.persist(company);
            em.flush();
        } else {
            company = TestUtil.findAll(em, Company.class).get(0);
        }
//        transaction.setCompany(company);
        // Add required entity
        PaymentGateway paymentGateway;
        if (TestUtil.findAll(em, PaymentGateway.class).isEmpty()) {
            paymentGateway = PaymentGatewayResourceIT.createEntity(em);
            em.persist(paymentGateway);
            em.flush();
        } else {
            paymentGateway = TestUtil.findAll(em, PaymentGateway.class).get(0);
        }
        transaction.setPaymentGateway(paymentGateway);
        return transaction;
    }

    @BeforeEach
    public void initTest() {
        transaction = createEntity(em);
    }

    @Test
    @Transactional
    void createTransaction() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();
        // Create the Transaction
        TransactionDTO transactionDTO = transactionService.toDto(transaction);
        restTransactionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isCreated());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTxCode()).isEqualTo(DEFAULT_TX_CODE);
        assertThat(testTransaction.getProviderTxCode()).isEqualTo(DEFAULT_PROVIDER_TX_CODE);
//        assertThat(testTransaction.getCustomerUserId()).isEqualTo(DEFAULT_CUSTOMER_USER_ID);
//        assertThat(testTransaction.getGloss()).isEqualTo(DEFAULT_GLOSS);
        assertThat(testTransaction.getAmount()).isEqualByComparingTo(DEFAULT_AMOUNT);
        assertThat(testTransaction.getCurrencyType()).isEqualTo(DEFAULT_CURRENCY_TYPE);
        assertThat(testTransaction.getChangeType()).isEqualByComparingTo(DEFAULT_CHANGE_TYPE);
        assertThat(testTransaction.getAdditionalInformation()).isEqualTo(DEFAULT_ADDITIONAL_INFORMATION);
    }

    @Test
    @Transactional
    void checkTxCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = transactionRepository.findAll().size();
        // set the field null
        transaction.setTxCode(null);

        // Create the Transaction, which fails.
        TransactionDTO transactionDTO = transactionService.toDto(transaction);

        restTransactionMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isBadRequest());

        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTransactions() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get all the transactionList
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(transaction.getId().intValue())))
            .andExpect(jsonPath("$.[*].txCode").value(hasItem(DEFAULT_TX_CODE)))
            .andExpect(jsonPath("$.[*].providerTxCode").value(hasItem(DEFAULT_PROVIDER_TX_CODE)))
            .andExpect(jsonPath("$.[*].customerUserId").value(hasItem(DEFAULT_CUSTOMER_USER_ID)))
            .andExpect(jsonPath("$.[*].gloss").value(hasItem(DEFAULT_GLOSS)))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currencyType").value(hasItem(DEFAULT_CURRENCY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].changeType").value(hasItem(sameNumber(DEFAULT_CHANGE_TYPE))))
            .andExpect(jsonPath("$.[*].additionalInformation").value(hasItem(DEFAULT_ADDITIONAL_INFORMATION)));
    }

    @Test
    @Transactional
    void getTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        // Get the transaction
        restTransactionMockMvc
            .perform(get(ENTITY_API_URL_ID, transaction.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(transaction.getId().intValue()))
            .andExpect(jsonPath("$.txCode").value(DEFAULT_TX_CODE))
            .andExpect(jsonPath("$.providerTxCode").value(DEFAULT_PROVIDER_TX_CODE))
            .andExpect(jsonPath("$.customerUserId").value(DEFAULT_CUSTOMER_USER_ID))
            .andExpect(jsonPath("$.gloss").value(DEFAULT_GLOSS))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.currencyType").value(DEFAULT_CURRENCY_TYPE.toString()))
            .andExpect(jsonPath("$.changeType").value(sameNumber(DEFAULT_CHANGE_TYPE)))
            .andExpect(jsonPath("$.additionalInformation").value(DEFAULT_ADDITIONAL_INFORMATION));
    }

    @Test
    @Transactional
    void getNonExistingTransaction() throws Exception {
        // Get the transaction
        restTransactionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewTransaction() throws Exception {
        // Initialize the database
        Transaction updatedTransaction = transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        updatedTransaction = Transaction.builder()
            .id(updatedTransaction.getId())
            .txCode(UPDATED_TX_CODE)
            .providerTxCode(UPDATED_PROVIDER_TX_CODE)
//            .customerUserId(UPDATED_CUSTOMER_USER_ID)
//            .gloss(UPDATED_GLOSS)
            .amount(UPDATED_AMOUNT)
            .currencyType(UPDATED_CURRENCY_TYPE)
            .changeType(UPDATED_CHANGE_TYPE)
            .additionalInformation(UPDATED_ADDITIONAL_INFORMATION)
//            .company(updatedTransaction.getCompany())
            .paymentGateway(updatedTransaction.getPaymentGateway())
            .build();
        TransactionDTO transactionDTO = transactionService.toDto(updatedTransaction);

        restTransactionMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
        Transaction testTransaction = transactionList.get(transactionList.size() - 1);
        assertThat(testTransaction.getTxCode()).isEqualTo(UPDATED_TX_CODE);
        assertThat(testTransaction.getProviderTxCode()).isEqualTo(UPDATED_PROVIDER_TX_CODE);
//        assertThat(testTransaction.getCustomerUserId()).isEqualTo(UPDATED_CUSTOMER_USER_ID);
//        assertThat(testTransaction.getGloss()).isEqualTo(UPDATED_GLOSS);
        assertThat(testTransaction.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testTransaction.getCurrencyType()).isEqualTo(UPDATED_CURRENCY_TYPE);
        assertThat(testTransaction.getChangeType()).isEqualTo(UPDATED_CHANGE_TYPE);
        assertThat(testTransaction.getAdditionalInformation()).isEqualTo(UPDATED_ADDITIONAL_INFORMATION);
    }

    @Test
    @Transactional
    void putNonExistingTransaction() throws Exception {
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();
        transaction.setId(count.incrementAndGet());

        // Create the Transaction
        TransactionDTO transactionDTO = transactionService.toDto(transaction);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTransactionMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(transactionDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the Transaction in the database
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTransaction() throws Exception {
        // Initialize the database
        transactionRepository.saveAndFlush(transaction);

        int databaseSizeBeforeDelete = transactionRepository.findAll().size();

        // Delete the transaction
        restTransactionMockMvc
            .perform(delete(ENTITY_API_URL_ID, transaction.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Transaction> transactionList = transactionRepository.findAll();
        assertThat(transactionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
