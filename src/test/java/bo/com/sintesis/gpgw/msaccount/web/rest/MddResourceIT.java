package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Mdd;
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
class MddResourceIT {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_FORMAT = "AAAAAAAAAA";
    private static final String UPDATED_FORMAT = "BBBBBBBBBB";

    private static final String DEFAULT_EXAMPLE = "AAAAAAAAAA";
    private static final String UPDATED_EXAMPLE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_OPTIONAL = false;
    private static final Boolean UPDATED_OPTIONAL = true;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/mdds";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private MddRepository mddRepository;

    @Autowired
    private MockMvc restMddMockMvc;

    private Mdd mdd;

    public static Mdd createEntity() {
        return Mdd.builder()
            .code(DEFAULT_CODE)
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .format(DEFAULT_FORMAT)
            .example(DEFAULT_EXAMPLE)
            .optional(DEFAULT_OPTIONAL)
            .active(DEFAULT_ACTIVE)
            .build();
    }

    @BeforeEach
    public void initTest() {
        mdd = createEntity();
    }

    @Test
    @Transactional
    void createMdd() throws Exception {
        int databaseSizeBeforeCreate = mddRepository.findAll().size();
        // Create the Mdd
        restMddMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(mdd)))
            .andExpect(status().isCreated());

        // Validate the Mdd in the database
        List<Mdd> mddList = mddRepository.findAll();
        assertThat(mddList).hasSize(databaseSizeBeforeCreate + 1);
        Mdd testMdd = mddList.get(mddList.size() - 1);
        assertThat(testMdd.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testMdd.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testMdd.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testMdd.getFormat()).isEqualTo(DEFAULT_FORMAT);
        assertThat(testMdd.getExample()).isEqualTo(DEFAULT_EXAMPLE);
        assertThat(testMdd.getOptional()).isEqualTo(DEFAULT_OPTIONAL);
        assertThat(testMdd.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = mddRepository.findAll().size();
        // set the field null
        mdd.setCode(null);

        restMddMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(mdd)))
            .andExpect(status().isBadRequest());

        List<Mdd> mddList = mddRepository.findAll();
        assertThat(mddList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void putNewMdd() throws Exception {
        // Initialize the database
        mddRepository.saveAndFlush(mdd);

        int databaseSizeBeforeUpdate = mddRepository.findAll().size();

        Mdd updatedMdd = Mdd.builder()
            .id(mdd.getId())
            .code(UPDATED_CODE)
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .format(UPDATED_FORMAT)
            .example(UPDATED_EXAMPLE)
            .optional(UPDATED_OPTIONAL)
            .active(UPDATED_ACTIVE)
            .build();

        restMddMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(updatedMdd)))
            .andExpect(status().isOk());

        // Validate the Mdd in the database
        List<Mdd> mddList = mddRepository.findAll();
        assertThat(mddList).hasSize(databaseSizeBeforeUpdate);
        Mdd testMdd = mddList.get(mddList.size() - 1);
        assertThat(testMdd.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testMdd.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testMdd.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testMdd.getFormat()).isEqualTo(UPDATED_FORMAT);
        assertThat(testMdd.getExample()).isEqualTo(UPDATED_EXAMPLE);
        assertThat(testMdd.getOptional()).isEqualTo(UPDATED_OPTIONAL);
        assertThat(testMdd.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingMdd() throws Exception {
        int databaseSizeBeforeUpdate = mddRepository.findAll().size();
        mdd.setId(count.incrementAndGet());

        restMddMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(mdd))
            )
            .andExpect(status().isNotFound());

        // Validate the Mdd in the database
        List<Mdd> mddList = mddRepository.findAll();
        assertThat(mddList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteMdd() throws Exception {
        // Initialize the database
        mddRepository.saveAndFlush(mdd);

        int databaseSizeBeforeDelete = mddRepository.findAll().size();

        // Delete the mdd
        restMddMockMvc
            .perform(delete(ENTITY_API_URL_ID, mdd.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Mdd> mddList = mddRepository.findAll();
        assertThat(mddList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getMdd() throws Exception {
        // Initialize the database
        mddRepository.saveAndFlush(mdd);

        // Get the mdd
        restMddMockMvc
            .perform(get(ENTITY_API_URL_ID, mdd.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(mdd.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.format").value(DEFAULT_FORMAT))
            .andExpect(jsonPath("$.example").value(DEFAULT_EXAMPLE))
            .andExpect(jsonPath("$.optional").value(DEFAULT_OPTIONAL))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingMdd() throws Exception {
        // Get the mdd
        restMddMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getAllMdds() throws Exception {
        // Initialize the database
        mddRepository.saveAndFlush(mdd);

        // Get all the mddList
        restMddMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(mdd.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].format").value(hasItem(DEFAULT_FORMAT)))
            .andExpect(jsonPath("$.[*].example").value(hasItem(DEFAULT_EXAMPLE)))
            .andExpect(jsonPath("$.[*].optional").value(hasItem(DEFAULT_OPTIONAL)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }
}
