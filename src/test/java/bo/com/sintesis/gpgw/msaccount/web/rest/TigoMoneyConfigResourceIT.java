package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.TigoMoneyConfig;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.TigoMoneyConfigRepository;
import bo.com.bisa.gpgw.msaccount.service.TigoMoneyConfigService;
import bo.com.bisa.gpgw.msaccount.service.dto.TigoMoneyConfigDTO;
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
class TigoMoneyConfigResourceIT {

    private static final String DEFAULT_IDENTIFICATION_KEY = "AAAAAAAAAA";
    private static final String UPDATED_IDENTIFICATION_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_ENCRIPTION_KEY = "AAAAAAAAAA";
    private static final String UPDATED_ENCRIPTION_KEY = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/tigo-money-configs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private TigoMoneyConfigRepository tigoMoneyConfigRepository;

    @Autowired
    private TigoMoneyConfigService tigoMoneyConfigService;

    @Autowired
    private MockMvc restTigoMoneyConfigMockMvc;

    private TigoMoneyConfig tigoMoneyConfig;

    public static TigoMoneyConfig createEntity() {
        return TigoMoneyConfig.builder()
            .identificationKey(DEFAULT_IDENTIFICATION_KEY)
            .encriptionKey(DEFAULT_ENCRIPTION_KEY)
            .active(DEFAULT_ACTIVE)
            .build();
    }

    @BeforeEach
    public void initTest() {
        tigoMoneyConfig = createEntity();
    }

    @Test
    @Transactional
    void createTigoMoneyConfig() throws Exception {
        int databaseSizeBeforeCreate = tigoMoneyConfigRepository.findAll().size();
        // Create the TigoMoneyConfig
        TigoMoneyConfigDTO tigoMoneyConfigDTO = tigoMoneyConfigService.toDto(tigoMoneyConfig);
        restTigoMoneyConfigMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tigoMoneyConfigDTO))
            )
            .andExpect(status().isCreated());

        // Validate the TigoMoneyConfig in the database
        List<TigoMoneyConfig> tigoMoneyConfigList = tigoMoneyConfigRepository.findAll();
        assertThat(tigoMoneyConfigList).hasSize(databaseSizeBeforeCreate + 1);
        TigoMoneyConfig testTigoMoneyConfig = tigoMoneyConfigList.get(tigoMoneyConfigList.size() - 1);
        assertThat(testTigoMoneyConfig.getIdentificationKey()).isEqualTo(DEFAULT_IDENTIFICATION_KEY);
        assertThat(testTigoMoneyConfig.getEncriptionKey()).isEqualTo(DEFAULT_ENCRIPTION_KEY);
        assertThat(testTigoMoneyConfig.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkEncriptionKeyIsRequired() throws Exception {
        int databaseSizeBeforeTest = tigoMoneyConfigRepository.findAll().size();
        // set the field null
        tigoMoneyConfig.setEncriptionKey(null);

        // Create the TigoMoneyConfig, which fails.
        TigoMoneyConfigDTO tigoMoneyConfigDTO = tigoMoneyConfigService.toDto(tigoMoneyConfig);

        restTigoMoneyConfigMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(tigoMoneyConfigDTO))
            )
            .andExpect(status().isBadRequest());

        List<TigoMoneyConfig> tigoMoneyConfigList = tigoMoneyConfigRepository.findAll();
        assertThat(tigoMoneyConfigList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTigoMoneyConfigs() throws Exception {
        // Initialize the database
        tigoMoneyConfigRepository.saveAndFlush(tigoMoneyConfig);

        // Get all the tigoMoneyConfigList
        restTigoMoneyConfigMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(tigoMoneyConfig.getId().intValue())))
            .andExpect(jsonPath("$.[*].identificationKey").value(hasItem(DEFAULT_IDENTIFICATION_KEY)))
            .andExpect(jsonPath("$.[*].encriptionKey").value(hasItem(DEFAULT_ENCRIPTION_KEY)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

    @Test
    @Transactional
    void getTigoMoneyConfig() throws Exception {
        // Initialize the database
        tigoMoneyConfigRepository.saveAndFlush(tigoMoneyConfig);

        // Get the tigoMoneyConfig
        restTigoMoneyConfigMockMvc
            .perform(get(ENTITY_API_URL_ID, tigoMoneyConfig.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(tigoMoneyConfig.getId().intValue()))
            .andExpect(jsonPath("$.identificationKey").value(DEFAULT_IDENTIFICATION_KEY))
            .andExpect(jsonPath("$.encriptionKey").value(DEFAULT_ENCRIPTION_KEY))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingTigoMoneyConfig() throws Exception {
        // Get the tigoMoneyConfig
        restTigoMoneyConfigMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewTigoMoneyConfig() throws Exception {
        // Initialize the database
        TigoMoneyConfig updatedTigoMoneyConfig = tigoMoneyConfigRepository.saveAndFlush(tigoMoneyConfig);

        int databaseSizeBeforeUpdate = tigoMoneyConfigRepository.findAll().size();

        updatedTigoMoneyConfig = TigoMoneyConfig.builder()
            .id(updatedTigoMoneyConfig.getId())
            .identificationKey(UPDATED_IDENTIFICATION_KEY).encriptionKey(UPDATED_ENCRIPTION_KEY).active(UPDATED_ACTIVE)
            .build();
        TigoMoneyConfigDTO tigoMoneyConfigDTO = tigoMoneyConfigService.toDto(updatedTigoMoneyConfig);

        restTigoMoneyConfigMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(tigoMoneyConfigDTO))
            )
            .andExpect(status().isOk());

        // Validate the TigoMoneyConfig in the database
        List<TigoMoneyConfig> tigoMoneyConfigList = tigoMoneyConfigRepository.findAll();
        assertThat(tigoMoneyConfigList).hasSize(databaseSizeBeforeUpdate);
        TigoMoneyConfig testTigoMoneyConfig = tigoMoneyConfigList.get(tigoMoneyConfigList.size() - 1);
        assertThat(testTigoMoneyConfig.getIdentificationKey()).isEqualTo(UPDATED_IDENTIFICATION_KEY);
        assertThat(testTigoMoneyConfig.getEncriptionKey()).isEqualTo(UPDATED_ENCRIPTION_KEY);
        assertThat(testTigoMoneyConfig.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingTigoMoneyConfig() throws Exception {
        int databaseSizeBeforeUpdate = tigoMoneyConfigRepository.findAll().size();
        tigoMoneyConfig.setId(count.incrementAndGet());

        // Create the TigoMoneyConfig
        TigoMoneyConfigDTO tigoMoneyConfigDTO = tigoMoneyConfigService.toDto(tigoMoneyConfig);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTigoMoneyConfigMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(tigoMoneyConfigDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the TigoMoneyConfig in the database
        List<TigoMoneyConfig> tigoMoneyConfigList = tigoMoneyConfigRepository.findAll();
        assertThat(tigoMoneyConfigList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTigoMoneyConfig() throws Exception {
        // Initialize the database
        tigoMoneyConfigRepository.saveAndFlush(tigoMoneyConfig);

        int databaseSizeBeforeDelete = tigoMoneyConfigRepository.findAll().size();

        // Delete the tigoMoneyConfig
        restTigoMoneyConfigMockMvc
            .perform(delete(ENTITY_API_URL_ID, tigoMoneyConfig.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<TigoMoneyConfig> tigoMoneyConfigList = tigoMoneyConfigRepository.findAll();
        assertThat(tigoMoneyConfigList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
