package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.DebitConfig;
import bo.com.bisa.gpgw.domain.Provider;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.DebitConfigRepository;
import bo.com.bisa.gpgw.msaccount.service.DebitConfigService;
import bo.com.bisa.gpgw.msaccount.service.dto.DebitConfigDTO;
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
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DebitConfigResourceIT {

    private static final Integer DEFAULT_MAX_FAILED_ATTEMP = 0;
    private static final Integer UPDATED_MAX_FAILED_ATTEMP = 1;

    private static final Integer DEFAULT_MAX_DEBIT_ATTEMP = 0;
    private static final Integer UPDATED_MAX_DEBIT_ATTEMP = 1;

    private static final String ENTITY_API_URL = "/api/debit-configs";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private DebitConfigRepository debitConfigRepository;

    @Autowired
    private DebitConfigService debitConfigService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDebitConfigMockMvc;

    private DebitConfig debitConfig;

    public static DebitConfig createEntity(EntityManager em) {
        DebitConfig debitConfig = DebitConfig.builder().maxFailedAttemp(DEFAULT_MAX_FAILED_ATTEMP).maxDebitAttemp(DEFAULT_MAX_DEBIT_ATTEMP).build();
        // Add required entity
        Provider provider;
        if (TestUtil.findAll(em, Provider.class).isEmpty()) {
            provider = ProviderResourceIT.createEntity(em);
            em.persist(provider);
            em.flush();
        } else {
            provider = TestUtil.findAll(em, Provider.class).get(0);
        }
        debitConfig.setProvider(provider);
        return debitConfig;
    }

    @BeforeEach
    public void initTest() {
        debitConfig = createEntity(em);
    }

    @Test
    @Transactional
    void createDebitConfig() throws Exception {
        int databaseSizeBeforeCreate = debitConfigRepository.findAll().size();
        // Create the DebitConfig
        DebitConfigDTO debitConfigDTO = debitConfigService.toDto(debitConfig);
        restDebitConfigMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(debitConfigDTO))
            )
            .andExpect(status().isCreated());

        // Validate the DebitConfig in the database
        List<DebitConfig> debitConfigList = debitConfigRepository.findAll();
        assertThat(debitConfigList).hasSize(databaseSizeBeforeCreate + 1);
        DebitConfig testDebitConfig = debitConfigList.get(debitConfigList.size() - 1);
        assertThat(testDebitConfig.getMaxFailedAttemp()).isEqualTo(DEFAULT_MAX_FAILED_ATTEMP);
        assertThat(testDebitConfig.getMaxDebitAttemp()).isEqualTo(DEFAULT_MAX_DEBIT_ATTEMP);
    }

    @Test
    @Transactional
    void checkMaxFailedAttempIsRequired() throws Exception {
        int databaseSizeBeforeTest = debitConfigRepository.findAll().size();
        // set the field null
        debitConfig.setMaxFailedAttemp(null);

        // Create the DebitConfig, which fails.
        DebitConfigDTO debitConfigDTO = debitConfigService.toDto(debitConfig);

        restDebitConfigMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(debitConfigDTO))
            )
            .andExpect(status().isBadRequest());

        List<DebitConfig> debitConfigList = debitConfigRepository.findAll();
        assertThat(debitConfigList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDebitConfigs() throws Exception {
        // Initialize the database
        debitConfigRepository.saveAndFlush(debitConfig);

        // Get all the debitConfigList
        restDebitConfigMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(debitConfig.getId().intValue())))
            .andExpect(jsonPath("$.[*].maxFailedAttemp").value(hasItem(DEFAULT_MAX_FAILED_ATTEMP)))
            .andExpect(jsonPath("$.[*].maxDebitAttemp").value(hasItem(DEFAULT_MAX_DEBIT_ATTEMP)));
    }

    @Test
    @Transactional
    void getDebitConfig() throws Exception {
        // Initialize the database
        debitConfigRepository.saveAndFlush(debitConfig);

        // Get the debitConfig
        restDebitConfigMockMvc
            .perform(get(ENTITY_API_URL_ID, debitConfig.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(debitConfig.getId().intValue()))
            .andExpect(jsonPath("$.maxFailedAttemp").value(DEFAULT_MAX_FAILED_ATTEMP))
            .andExpect(jsonPath("$.maxDebitAttemp").value(DEFAULT_MAX_DEBIT_ATTEMP));
    }

    @Test
    @Transactional
    void getNonExistingDebitConfig() throws Exception {
        // Get the debitConfig
        restDebitConfigMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewDebitConfig() throws Exception {
        // Initialize the database
        DebitConfig updatedDebitConfig = debitConfigRepository.saveAndFlush(debitConfig);

        int databaseSizeBeforeUpdate = debitConfigRepository.findAll().size();

        updatedDebitConfig = DebitConfig.builder()
            .id(updatedDebitConfig.getId())
            .maxFailedAttemp(UPDATED_MAX_FAILED_ATTEMP)
            .maxDebitAttemp(UPDATED_MAX_DEBIT_ATTEMP)
            .provider(updatedDebitConfig.getProvider())
            .build();
        DebitConfigDTO debitConfigDTO = debitConfigService.toDto(updatedDebitConfig);

        restDebitConfigMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(debitConfigDTO))
            )
            .andExpect(status().isOk());

        // Validate the DebitConfig in the database
        List<DebitConfig> debitConfigList = debitConfigRepository.findAll();
        assertThat(debitConfigList).hasSize(databaseSizeBeforeUpdate);
        DebitConfig testDebitConfig = debitConfigList.get(debitConfigList.size() - 1);
        assertThat(testDebitConfig.getMaxFailedAttemp()).isEqualTo(UPDATED_MAX_FAILED_ATTEMP);
        assertThat(testDebitConfig.getMaxDebitAttemp()).isEqualTo(UPDATED_MAX_DEBIT_ATTEMP);
    }

    @Test
    @Transactional
    void putNonExistingDebitConfig() throws Exception {
        int databaseSizeBeforeUpdate = debitConfigRepository.findAll().size();
        debitConfig.setId(count.incrementAndGet());

        // Create the DebitConfig
        DebitConfigDTO debitConfigDTO = debitConfigService.toDto(debitConfig);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDebitConfigMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(debitConfigDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the DebitConfig in the database
        List<DebitConfig> debitConfigList = debitConfigRepository.findAll();
        assertThat(debitConfigList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDebitConfig() throws Exception {
        // Initialize the database
        debitConfigRepository.saveAndFlush(debitConfig);

        int databaseSizeBeforeDelete = debitConfigRepository.findAll().size();

        // Delete the debitConfig
        restDebitConfigMockMvc
            .perform(delete(ENTITY_API_URL_ID, debitConfig.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<DebitConfig> debitConfigList = debitConfigRepository.findAll();
        assertThat(debitConfigList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
