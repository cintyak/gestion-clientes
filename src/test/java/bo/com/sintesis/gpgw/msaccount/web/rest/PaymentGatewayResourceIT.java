package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Mdd;
import bo.com.bisa.gpgw.domain.PaymentGateway;
import bo.com.bisa.gpgw.domain.PaymentMode;
import bo.com.bisa.gpgw.domain.enumeration.PaymentModalityEnum;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.PaymentGatewayRepository;
import bo.com.bisa.gpgw.msaccount.service.PaymentGatewayService;
import bo.com.bisa.gpgw.msaccount.service.dto.PaymentGatewayDTO;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PaymentGatewayResourceIT {

    private static final PaymentModalityEnum DEFAULT_MODALITY = PaymentModalityEnum.FACILITATOR;
    private static final PaymentModalityEnum UPDATED_MODALITY = PaymentModalityEnum.AGGREGATOR;

    private static final Boolean DEFAULT_AUTOMATED_DEBIT = false;
    private static final Boolean UPDATED_AUTOMATED_DEBIT = true;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/payment-gateways";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private PaymentGatewayRepository paymentGatewayRepository;

    @Autowired
    private PaymentGatewayService paymentGatewayService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPaymentGatewayMockMvc;

    private PaymentGateway paymentGateway;

    private PaymentGatewayDTO paymentGatewayDTO;

    public static PaymentGateway createEntity(EntityManager em) {
        PaymentGateway paymentGateway = PaymentGateway.builder()
            .modality(DEFAULT_MODALITY)
            .automatedDebit(DEFAULT_AUTOMATED_DEBIT)
            .active(DEFAULT_ACTIVE)
            .build();
        // Add required entity
//        ServiceCompany serviceCompany;
//        if (TestUtil.findAll(em, ServiceCompany.class).isEmpty()) {
//            serviceCompany = ServiceCompanyResourceIT.createEntity(em);
//            em.persist(serviceCompany);
//            em.flush();
//        } else {
//            serviceCompany = TestUtil.findAll(em, ServiceCompany.class).get(0);
//        }
//        paymentGateway.setServiceCompany(serviceCompany);
        // Add required entity
        PaymentMode paymentMode;
        if (TestUtil.findAll(em, PaymentMode.class).isEmpty()) {
            paymentMode = PaymentModeResourceIT.createEntity(em);
            em.persist(paymentMode);
            em.flush();
        } else {
            paymentMode = TestUtil.findAll(em, PaymentMode.class).get(0);
        }
        paymentGateway.setPaymentMode(paymentMode);
        List<Mdd> mddList = new ArrayList<>();
        Mdd mdd;
        if (TestUtil.findAll(em, Mdd.class).isEmpty()) {
            mdd = MddResourceIT.createEntity();
            em.persist(mdd);
            em.flush();
        } else {
            mdd = TestUtil.findAll(em, Mdd.class).get(0);
        }
        mddList.add(mdd);
        Set<Mdd> mdds = new HashSet<>(mddList);
        paymentGateway.setMdds(mdds);
        return paymentGateway;
    }

    @BeforeEach
    public void initTest() {
        paymentGateway = createEntity(em);
        paymentGatewayDTO = paymentGatewayService.toDto(paymentGateway);
    }

    @Test
    @Transactional
    void createPaymentGateway() throws Exception {
        int databaseSizeBeforeCreate = paymentGatewayRepository.findAll().size();
        // Create the PaymentGateway
        restPaymentGatewayMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(paymentGatewayDTO))
            )
            .andExpect(status().isCreated());

        // Validate the PaymentGateway in the database
        List<PaymentGateway> paymentGatewayList = paymentGatewayRepository.findAll();
        assertThat(paymentGatewayList).hasSize(databaseSizeBeforeCreate + 1);
        PaymentGateway testPaymentGateway = paymentGatewayList.get(paymentGatewayList.size() - 1);
        assertThat(testPaymentGateway.getModality()).isEqualTo(DEFAULT_MODALITY);
        assertThat(testPaymentGateway.getAutomatedDebit()).isEqualTo(DEFAULT_AUTOMATED_DEBIT);
        assertThat(testPaymentGateway.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void createPaymentGatewayWithExistingId() throws Exception {
        // Create the PaymentGateway with an existing ID
        paymentGateway.setId(1L);

        int databaseSizeBeforeCreate = paymentGatewayRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPaymentGatewayMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(paymentGateway))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentGateway in the database
        List<PaymentGateway> paymentGatewayList = paymentGatewayRepository.findAll();
        assertThat(paymentGatewayList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkModalityIsRequired() throws Exception {
        int databaseSizeBeforeTest = paymentGatewayRepository.findAll().size();
        // set the field null
        paymentGateway.setModality(null);

        // Create the PaymentGateway, which fails.

        restPaymentGatewayMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(paymentGateway))
            )
            .andExpect(status().isBadRequest());

        List<PaymentGateway> paymentGatewayList = paymentGatewayRepository.findAll();
        assertThat(paymentGatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAutomatedDebitIsRequired() throws Exception {
        int databaseSizeBeforeTest = paymentGatewayRepository.findAll().size();
        // set the field null
        paymentGateway.setAutomatedDebit(null);

        // Create the PaymentGateway, which fails.

        restPaymentGatewayMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(paymentGateway))
            )
            .andExpect(status().isBadRequest());

        List<PaymentGateway> paymentGatewayList = paymentGatewayRepository.findAll();
        assertThat(paymentGatewayList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPaymentGateways() throws Exception {
        // Initialize the database
        paymentGatewayRepository.saveAndFlush(paymentGateway);

        // Get all the paymentGatewayList
        restPaymentGatewayMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(paymentGateway.getId().intValue())))
            .andExpect(jsonPath("$.[*].modality").value(hasItem(DEFAULT_MODALITY.toString())))
            .andExpect(jsonPath("$.[*].automatedDebit").value(hasItem(DEFAULT_AUTOMATED_DEBIT)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getPaymentGateway() throws Exception {
        // Initialize the database
        paymentGatewayRepository.saveAndFlush(paymentGateway);

        // Get the paymentGateway
        restPaymentGatewayMockMvc
            .perform(get(ENTITY_API_URL_ID, paymentGateway.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(paymentGateway.getId().intValue()))
            .andExpect(jsonPath("$.modality").value(DEFAULT_MODALITY.toString()))
            .andExpect(jsonPath("$.automatedDebit").value(DEFAULT_AUTOMATED_DEBIT))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingPaymentGateway() throws Exception {
        // Get the paymentGateway
        restPaymentGatewayMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPaymentGateway() throws Exception {
        // Initialize the database
        PaymentGateway updatedPaymentGateway = paymentGatewayRepository.saveAndFlush(paymentGateway);

        int databaseSizeBeforeUpdate = paymentGatewayRepository.findAll().size();

        PaymentGatewayDTO paymentUpdate = new PaymentGatewayDTO();

        paymentUpdate.setId(updatedPaymentGateway.getId());
        paymentUpdate.setModality(UPDATED_MODALITY);
        paymentUpdate.setAutomatedDebit(UPDATED_AUTOMATED_DEBIT);
        paymentUpdate.setActive(UPDATED_ACTIVE);
        paymentUpdate.setPaymentModeId(updatedPaymentGateway.getPaymentMode().getId());
//        paymentUpdate.setMddIds(updatedPaymentGateway.getMdds().stream().map(mdd -> mdd.getId()).collect(Collectors.toSet()));

        restPaymentGatewayMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(paymentUpdate))
            )
            .andExpect(status().isOk());

        // Validate the PaymentGateway in the database
        List<PaymentGateway> paymentGatewayList = paymentGatewayRepository.findAll();
        assertThat(paymentGatewayList).hasSize(databaseSizeBeforeUpdate);
        PaymentGateway testPaymentGateway = paymentGatewayList.get(paymentGatewayList.size() - 1);
        assertThat(testPaymentGateway.getModality()).isEqualTo(UPDATED_MODALITY);
        assertThat(testPaymentGateway.getAutomatedDebit()).isEqualTo(UPDATED_AUTOMATED_DEBIT);
        assertThat(testPaymentGateway.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingPaymentGateway() throws Exception {
        int databaseSizeBeforeUpdate = paymentGatewayRepository.findAll().size();
        paymentGateway.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPaymentGatewayMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(paymentGateway))
            )
            .andExpect(status().isBadRequest());

        // Validate the PaymentGateway in the database
        List<PaymentGateway> paymentGatewayList = paymentGatewayRepository.findAll();
        assertThat(paymentGatewayList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePaymentGateway() throws Exception {
        // Initialize the database
        paymentGatewayRepository.saveAndFlush(paymentGateway);

        int databaseSizeBeforeDelete = paymentGatewayRepository.findAll().size();

        // Delete the paymentGateway
        restPaymentGatewayMockMvc
            .perform(delete(ENTITY_API_URL_ID, paymentGateway.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PaymentGateway> paymentGatewayList = paymentGatewayRepository.findAll();
        assertThat(paymentGatewayList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
