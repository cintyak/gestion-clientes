package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.BcpConfig;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.service.BcpConfigService;
import bo.com.bisa.gpgw.msaccount.service.dto.BcpConfigDTO;
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
class BcpConfigResourceIT {

    private static final String DEFAULT_BUSINESS_CODE = "AAAAAAAAAA";
    private static final String UPDATED_BUSINESS_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_SERVICE_CODE = "AAAAAAAAAA";
    private static final String UPDATED_SERVICE_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_PUBLIC_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_PUBLIC_TOKEN = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/bcp-configs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private BcpConfigRepository bcpConfigRepository;

    @Autowired
    private BcpConfigService bcpConfigService;

    @Autowired
    private MockMvc restBcpConfigMockMvc;

    private BcpConfig bcpConfig;

    public static BcpConfig createEntity() {
        return BcpConfig.builder()
            .businessCode(DEFAULT_BUSINESS_CODE)
            .serviceCode(DEFAULT_SERVICE_CODE)
            .publicToken(DEFAULT_PUBLIC_TOKEN)
            .active(DEFAULT_ACTIVE)
            .build();
    }

    @BeforeEach
    public void initTest() {
        bcpConfig = createEntity();
    }

    @Test
    @Transactional
    void createBcpConfig() throws Exception {
        int databaseSizeBeforeCreate = bcpConfigRepository.findAll().size();
        // Create the BcpConfig
        BcpConfigDTO bcpConfigDTO = bcpConfigService.toDto(bcpConfig);
        restBcpConfigMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bcpConfigDTO)))
            .andExpect(status().isCreated());

        // Validate the BcpConfig in the database
        List<BcpConfig> bcpConfigList = bcpConfigRepository.findAll();
        assertThat(bcpConfigList).hasSize(databaseSizeBeforeCreate + 1);
        BcpConfig testBcpConfig = bcpConfigList.get(bcpConfigList.size() - 1);
        assertThat(testBcpConfig.getBusinessCode()).isEqualTo(DEFAULT_BUSINESS_CODE);
        assertThat(testBcpConfig.getServiceCode()).isEqualTo(DEFAULT_SERVICE_CODE);
        assertThat(testBcpConfig.getPublicToken()).isEqualTo(DEFAULT_PUBLIC_TOKEN);
        assertThat(testBcpConfig.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkPublicTokenIsRequired() throws Exception {
        int databaseSizeBeforeTest = bcpConfigRepository.findAll().size();
        // set the field null
        bcpConfig.setPublicToken(null);

        // Create the BcpConfig, which fails.
        BcpConfigDTO bcpConfigDTO = bcpConfigService.toDto(bcpConfig);

        restBcpConfigMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(bcpConfigDTO)))
            .andExpect(status().isBadRequest());

        List<BcpConfig> bcpConfigList = bcpConfigRepository.findAll();
        assertThat(bcpConfigList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBcpConfigs() throws Exception {
        // Initialize the database
        bcpConfigRepository.saveAndFlush(bcpConfig);

        // Get all the bcpConfigList
        restBcpConfigMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(bcpConfig.getId().intValue())))
            .andExpect(jsonPath("$.[*].businessCode").value(hasItem(DEFAULT_BUSINESS_CODE)))
            .andExpect(jsonPath("$.[*].serviceCode").value(hasItem(DEFAULT_SERVICE_CODE)))
            .andExpect(jsonPath("$.[*].publicToken").value(hasItem(DEFAULT_PUBLIC_TOKEN)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getBcpConfig() throws Exception {
        // Initialize the database
        bcpConfigRepository.saveAndFlush(bcpConfig);

        // Get the bcpConfig
        restBcpConfigMockMvc
            .perform(get(ENTITY_API_URL_ID, bcpConfig.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(bcpConfig.getId().intValue()))
            .andExpect(jsonPath("$.businessCode").value(DEFAULT_BUSINESS_CODE))
            .andExpect(jsonPath("$.serviceCode").value(DEFAULT_SERVICE_CODE))
            .andExpect(jsonPath("$.publicToken").value(DEFAULT_PUBLIC_TOKEN))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingBcpConfig() throws Exception {
        // Get the bcpConfig
        restBcpConfigMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewBcpConfig() throws Exception {
        // Initialize the database
        BcpConfig updatedBcpConfig = bcpConfigRepository.saveAndFlush(bcpConfig);

        int databaseSizeBeforeUpdate = bcpConfigRepository.findAll().size();

        updatedBcpConfig = BcpConfig.builder()
            .id(updatedBcpConfig.getId())
            .businessCode(UPDATED_BUSINESS_CODE)
            .serviceCode(UPDATED_SERVICE_CODE)
            .publicToken(UPDATED_PUBLIC_TOKEN)
            .active(UPDATED_ACTIVE)
            .build();
        BcpConfigDTO bcpConfigDTO = bcpConfigService.toDto(updatedBcpConfig);

        restBcpConfigMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(bcpConfigDTO))
            )
            .andExpect(status().isOk());

        // Validate the BcpConfig in the database
        List<BcpConfig> bcpConfigList = bcpConfigRepository.findAll();
        assertThat(bcpConfigList).hasSize(databaseSizeBeforeUpdate);
        BcpConfig testBcpConfig = bcpConfigList.get(bcpConfigList.size() - 1);
        assertThat(testBcpConfig.getBusinessCode()).isEqualTo(UPDATED_BUSINESS_CODE);
        assertThat(testBcpConfig.getServiceCode()).isEqualTo(UPDATED_SERVICE_CODE);
        assertThat(testBcpConfig.getPublicToken()).isEqualTo(UPDATED_PUBLIC_TOKEN);
        assertThat(testBcpConfig.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingBcpConfig() throws Exception {
        int databaseSizeBeforeUpdate = bcpConfigRepository.findAll().size();
        bcpConfig.setId(count.incrementAndGet());

        // Create the BcpConfig
        BcpConfigDTO bcpConfigDTO = bcpConfigService.toDto(bcpConfig);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBcpConfigMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(bcpConfigDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the BcpConfig in the database
        List<BcpConfig> bcpConfigList = bcpConfigRepository.findAll();
        assertThat(bcpConfigList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBcpConfig() throws Exception {
        // Initialize the database
        bcpConfigRepository.saveAndFlush(bcpConfig);

        int databaseSizeBeforeDelete = bcpConfigRepository.findAll().size();

        // Delete the bcpConfig
        restBcpConfigMockMvc
            .perform(delete(ENTITY_API_URL_ID, bcpConfig.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<BcpConfig> bcpConfigList = bcpConfigRepository.findAll();
        assertThat(bcpConfigList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
