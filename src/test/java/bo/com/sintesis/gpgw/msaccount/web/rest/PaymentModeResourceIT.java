package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.PaymentMode;
import bo.com.bisa.gpgw.domain.Provider;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.PaymentModeRepository;
import bo.com.bisa.gpgw.msaccount.web.rest.request.PaymentModeReq;
import bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.persistence.EntityManager;
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
class PaymentModeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final byte[] DEFAULT_LOGO = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_LOGO = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_LOGO_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_LOGO_CONTENT_TYPE = "image/png";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/payment-modes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private PaymentModeRepository paymentModeRepository;

    @Autowired
    private MockMvc restPaymentModeMockMvc;

    @Autowired
    private EntityManager em;

    private PaymentMode paymentMode;

    private PaymentModeReq paymentModeReq;

    public static PaymentMode createEntity(EntityManager em) {
        PaymentMode paymentMode = PaymentMode.builder()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
//            .logo(DEFAULT_LOGO)
//            .logoContentType(DEFAULT_LOGO_CONTENT_TYPE)
            .active(DEFAULT_ACTIVE)
            .build();

        // Add required entity
        Provider provider;
        List<Provider> providers = TestUtil.findAll(em, Provider.class);
        if (providers.isEmpty()) {
            provider = Provider.builder()
                .name("Provider name test")
                .description("Provider description test")
                .active(true)
                .build();
            em.persist(provider);
            em.flush();
        } else {
            provider = providers.get(0);
        }
        paymentMode.setProvider(provider);
        return paymentMode;
    }

    public static PaymentModeReq createRequest(PaymentMode paymentMode) {
        return PaymentModeReq.builder()
            .name(paymentMode.getName())
            .description(paymentMode.getDescription())
//            .logo(paymentMode.getLogo())
//            .logoContentType(paymentMode.getLogoContentType())
            .active(paymentMode.getActive())
            .providerId(paymentMode.getProvider().getId())
            .build();
    }

    @BeforeEach
    public void initTest() {
        paymentMode = createEntity(em);
        paymentModeReq = createRequest(paymentMode);
    }

    @Test
    @Transactional
    void createPaymentMode() throws Exception {
        int databaseSizeBeforeCreate = paymentModeRepository.findAll().size();

        // Create the PaymentMode
        restPaymentModeMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(paymentModeReq)))
            .andExpect(status().isCreated());

        // Validate the PaymentMode in the database
        List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
        assertThat(paymentModeList).hasSize(databaseSizeBeforeCreate + 1);
        PaymentMode testPaymentMode = paymentModeList.get(paymentModeList.size() - 1);
        assertThat(testPaymentMode.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPaymentMode.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
//        assertThat(testPaymentMode.getLogo()).isEqualTo(DEFAULT_LOGO);
//        assertThat(testPaymentMode.getLogoContentType()).isEqualTo(DEFAULT_LOGO_CONTENT_TYPE);
        assertThat(testPaymentMode.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = paymentModeRepository.findAll().size();

        // set the field null
        paymentModeReq.setName(null);

        restPaymentModeMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(paymentModeReq)))
            .andExpect(status().isBadRequest());

        List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
        assertThat(paymentModeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void putNewPaymentMode() throws Exception {
        // Initialize the database
        PaymentMode updatedPaymentMode = paymentModeRepository.saveAndFlush(paymentMode);

        int databaseSizeBeforeUpdate = paymentModeRepository.findAll().size();

        PaymentModeReq paymentModeReq = PaymentModeReq.builder()
            .id(updatedPaymentMode.getId())
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
//            .logo(UPDATED_LOGO)
//            .logoContentType(UPDATED_LOGO_CONTENT_TYPE)
            .active(UPDATED_ACTIVE)
            .providerId(updatedPaymentMode.getProvider().getId())
            .build();

        restPaymentModeMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(paymentModeReq)))
            .andExpect(status().isOk());

        // Validate the PaymentMode in the database
        List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
        assertThat(paymentModeList).hasSize(databaseSizeBeforeUpdate);
        PaymentMode testPaymentMode = paymentModeList.get(paymentModeList.size() - 1);
        assertThat(testPaymentMode.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPaymentMode.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
//        assertThat(testPaymentMode.getLogo()).isEqualTo(UPDATED_LOGO);
//        assertThat(testPaymentMode.getLogoContentType()).isEqualTo(UPDATED_LOGO_CONTENT_TYPE);
        assertThat(testPaymentMode.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingPaymentMode() throws Exception {
        int databaseSizeBeforeUpdate = paymentModeRepository.findAll().size();
        paymentModeReq.setId(count.incrementAndGet());

        restPaymentModeMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(paymentModeReq))
            )
            .andExpect(status().isNotFound());

        // Validate the PaymentMode in the database
        List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
        assertThat(paymentModeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePaymentMode() throws Exception {
        // Initialize the database
        paymentModeRepository.saveAndFlush(paymentMode);

        int databaseSizeBeforeDelete = paymentModeRepository.findAll().size();

        // Delete the paymentMode
        restPaymentModeMockMvc
            .perform(delete(ENTITY_API_URL_ID, paymentMode.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
        assertThat(paymentModeList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getPaymentMode() throws Exception {
        // Initialize the database
        paymentModeRepository.saveAndFlush(paymentMode);

        // Get the paymentMode
        restPaymentModeMockMvc
            .perform(get(ENTITY_API_URL_ID, paymentMode.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(paymentMode.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.logoContentType").value(DEFAULT_LOGO_CONTENT_TYPE))
            .andExpect(jsonPath("$.logo").value(Base64Utils.encodeToString(DEFAULT_LOGO)))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingPaymentMode() throws Exception {
        // Get the paymentMode
        restPaymentModeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getAllPaymentModes() throws Exception {
        // Initialize the database
        paymentModeRepository.saveAndFlush(paymentMode);

        // Get all the paymentModeList
        restPaymentModeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(paymentMode.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].logoContentType").value(hasItem(DEFAULT_LOGO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO))))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }
}
