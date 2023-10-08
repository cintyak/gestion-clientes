package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Mid;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class MidResourceIT {

    private static final String DEFAULT_MERCHANT_ID = "AAAAAAAAAA";
    private static final String UPDATED_MERCHANT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_API_KEY = "AAAAAAAAAA";
    private static final String UPDATED_API_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_MERCHANT_SECRET_KEY = "AAAAAAAAAA";
    private static final String UPDATED_MERCHANT_SECRET_KEY = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/mids";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private MidRepository midRepository;

    @Autowired
    private MockMvc restMidMockMvc;

    private Mid mid;

    public static Mid createEntity() {
        return Mid.builder()
            .merchantId(DEFAULT_MERCHANT_ID)
            .apiKey(DEFAULT_API_KEY)
            .merchantSecretKey(DEFAULT_MERCHANT_SECRET_KEY)
            .active(DEFAULT_ACTIVE)
            .build();
    }

    @BeforeEach
    public void initTest() {
        mid = createEntity();
    }

    @Test
    @Transactional
    void createMid() throws Exception {
        int databaseSizeBeforeCreate = midRepository.findAll().size();
        // Create the Mid
        restMidMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(mid)))
            .andExpect(status().isCreated());

        // Validate the Mid in the database
        List<Mid> midList = midRepository.findAll();
        assertThat(midList).hasSize(databaseSizeBeforeCreate + 1);
        Mid testMid = midList.get(midList.size() - 1);
        assertThat(testMid.getMerchantId()).isEqualTo(DEFAULT_MERCHANT_ID);
        assertThat(testMid.getApiKey()).isEqualTo(DEFAULT_API_KEY);
        assertThat(testMid.getMerchantSecretKey()).isEqualTo(DEFAULT_MERCHANT_SECRET_KEY);
        assertThat(testMid.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkProfileIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = midRepository.findAll().size();
        // set the field null

        restMidMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(mid)))
            .andExpect(status().isBadRequest());

        List<Mid> midList = midRepository.findAll();
        assertThat(midList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void putNewMid() throws Exception {
        // Initialize the database
        midRepository.saveAndFlush(mid);

        int databaseSizeBeforeUpdate = midRepository.findAll().size();

        Mid updatedMid = Mid.builder()
            .id(mid.getId())
            .merchantId(UPDATED_MERCHANT_ID)
            .apiKey(UPDATED_API_KEY)
            .merchantSecretKey(UPDATED_MERCHANT_SECRET_KEY)
            .active(UPDATED_ACTIVE)
            .build();

        restMidMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(updatedMid)))
            .andExpect(status().isOk());

        // Validate the Mid in the database
        List<Mid> midList = midRepository.findAll();
        assertThat(midList).hasSize(databaseSizeBeforeUpdate);
        Mid testMid = midList.get(midList.size() - 1);
        assertThat(testMid.getMerchantId()).isEqualTo(UPDATED_MERCHANT_ID);
        assertThat(testMid.getApiKey()).isEqualTo(UPDATED_API_KEY);
        assertThat(testMid.getMerchantSecretKey()).isEqualTo(UPDATED_MERCHANT_SECRET_KEY);
        assertThat(testMid.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingMid() throws Exception {
        int databaseSizeBeforeUpdate = midRepository.findAll().size();
        mid.setId(count.incrementAndGet());

        restMidMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(mid)))
            .andExpect(status().isNotFound());

        // Validate the Mid in the database
        List<Mid> midList = midRepository.findAll();
        assertThat(midList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMid() throws Exception {
        // Initialize the database
        midRepository.saveAndFlush(mid);

        int databaseSizeBeforeDelete = midRepository.findAll().size();

        // Delete the mid
        restMidMockMvc
            .perform(delete(ENTITY_API_URL_ID, mid.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Mid> midList = midRepository.findAll();
        assertThat(midList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getMid() throws Exception {
        // Initialize the database
        midRepository.saveAndFlush(mid);

        // Get the mid
        restMidMockMvc
            .perform(get(ENTITY_API_URL_ID, mid.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(mid.getId().intValue()))
            .andExpect(jsonPath("$.merchantId").value(DEFAULT_MERCHANT_ID))
            .andExpect(jsonPath("$.apiKey").value(DEFAULT_API_KEY))
            .andExpect(jsonPath("$.merchantSecretKey").value(DEFAULT_MERCHANT_SECRET_KEY))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingMid() throws Exception {
        // Get the mid
        restMidMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getAllMids() throws Exception {
        // Initialize the database
        midRepository.saveAndFlush(mid);

        // Get all the midList
        restMidMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(mid.getId().intValue())))
            .andExpect(jsonPath("$.[*].merchantId").value(hasItem(DEFAULT_MERCHANT_ID)))
            .andExpect(jsonPath("$.[*].apiKey").value(hasItem(DEFAULT_API_KEY)))
            .andExpect(jsonPath("$.[*].merchantSecretKey").value(hasItem(DEFAULT_MERCHANT_SECRET_KEY)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }
}
