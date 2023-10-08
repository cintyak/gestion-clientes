package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Platform;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.PlatformRepository;
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
class PlatformResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/platforms";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private MockMvc restPlatformMockMvc;

    private Platform platform;

    public static Platform createEntity() {
        Platform platform = new Platform();
        platform.setName(DEFAULT_NAME);
        platform.setDescription(DEFAULT_DESCRIPTION);
        platform.setActive(DEFAULT_ACTIVE);
        return platform;
    }

    @BeforeEach
    public void initTest() {
        platform = createEntity();
    }

    @Test
    @Transactional
    void createPlatform() throws Exception {
        int databaseSizeBeforeCreate = platformRepository.findAll().size();
        // Create the Platform
        restPlatformMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(platform)))
            .andExpect(status().isCreated());

        // Validate the Platform in the database
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeCreate + 1);
        Platform testPlatform = platformList.get(platformList.size() - 1);
        assertThat(testPlatform.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPlatform.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testPlatform.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = platformRepository.findAll().size();
        // set the field null
        platform.setName(null);

        // Create the Platform, which fails.

        restPlatformMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(platform)))
            .andExpect(status().isBadRequest());

        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void putNewPlatform() throws Exception {
        // Initialize the database
        platformRepository.saveAndFlush(platform);

        int databaseSizeBeforeUpdate = platformRepository.findAll().size();

        // Update the platform
        Platform updatedPlatform = platformRepository.findById(platform.getId()).orElse(null);
        assertThat(updatedPlatform).isNotNull();

        updatedPlatform.setName(UPDATED_NAME);
        updatedPlatform.setDescription(UPDATED_DESCRIPTION);
        updatedPlatform.setActive(UPDATED_ACTIVE);

        restPlatformMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedPlatform))
            )
            .andExpect(status().isOk());

        // Validate the Platform in the database
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeUpdate);
        Platform testPlatform = platformList.get(platformList.size() - 1);
        assertThat(testPlatform.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPlatform.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testPlatform.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingPlatform() throws Exception {
        int databaseSizeBeforeUpdate = platformRepository.findAll().size();
        platform.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPlatformMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(platform))
            )
            .andExpect(status().isNotFound());

        // Validate the Platform in the database
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void getPlatform() throws Exception {
        // Initialize the database
        platformRepository.saveAndFlush(platform);

        // Get the platform
        restPlatformMockMvc
            .perform(get(ENTITY_API_URL_ID, platform.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(platform.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingPlatform() throws Exception {
        // Get the platform
        restPlatformMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void deletePlatform() throws Exception {
        // Initialize the database
        platformRepository.saveAndFlush(platform);

        int databaseSizeBeforeDelete = platformRepository.findAll().size();

        // Delete the platform
        restPlatformMockMvc
            .perform(delete(ENTITY_API_URL_ID, platform.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Platform> platformList = platformRepository.findAll();
        assertThat(platformList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getAllPlatforms() throws Exception {
        // Initialize the database
        platformRepository.saveAndFlush(platform);

        // Get all the platformList
        restPlatformMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(platform.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }
}
