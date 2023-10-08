package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Heading;
import bo.com.bisa.gpgw.domain.Mdd;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.web.rest.request.HeadingReq;
import bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
class HeadingResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/headings";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private HeadingRepository headingRepository;

    @Mock
    private HeadingRepository headingRepositoryMock;

    @Autowired
    private MockMvc restHeadingMockMvc;

    @Autowired
    private EntityManager em;

    private Heading heading;

    private HeadingReq headingReq;

    public static Heading createEntity(EntityManager em) {
        Heading heading = Heading.builder()
            .name(DEFAULT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .active(DEFAULT_ACTIVE)
            .build();

        // Add required entity
        Mdd mdd;
        List<Mdd> mdds = TestUtil.findAll(em, Mdd.class);
        if (mdds.isEmpty()) {
            mdd = Mdd.builder()
                .code("Mdd Code test")
                .name("Mdd Name test")
                .description("Mdd description test")
                .format("Mdd format test")
                .example("Mdd example test")
                .optional(false)
                .active(true)
                .build();
            em.persist(mdd);
            em.flush();
        } else {
            mdd = mdds.get(0);
        }
        heading.setMdds(Collections.singleton(mdd));
        return heading;
    }

    public static HeadingReq createRequest(Heading heading) {
        return HeadingReq.builder()
            .name(heading.getName())
            .description(heading.getDescription())
            .active(heading.getActive())
            .mddIds(heading.getMdds().stream().map(Mdd::getId).collect(Collectors.toSet()))
            .build();
    }

    @BeforeEach
    public void initTest() {
        heading = createEntity(em);
        headingReq = createRequest(heading);
    }

    @Test
    @Transactional
    void createHeading() throws Exception {
        int databaseSizeBeforeCreate = headingRepository.findAll().size();

        // Create the Heading
        restHeadingMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(headingReq)))
            .andExpect(status().isCreated());

        // Validate the Heading in the database
        List<Heading> headingList = headingRepository.findAll();
        assertThat(headingList).hasSize(databaseSizeBeforeCreate + 1);
        Heading testHeading = headingList.get(headingList.size() - 1);
        assertThat(testHeading.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testHeading.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testHeading.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = headingRepository.findAll().size();
        // set the field null
        headingReq.setName(null);

        restHeadingMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(headingReq)))
            .andExpect(status().isBadRequest());

        List<Heading> headingList = headingRepository.findAll();
        assertThat(headingList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void putNewHeading() throws Exception {
        // Initialize the database
        Heading updatedHeading = headingRepository.saveAndFlush(heading);

        int databaseSizeBeforeUpdate = headingRepository.findAll().size();

        HeadingReq headingReq = HeadingReq.builder()
            .id(updatedHeading.getId())
            .name(UPDATED_NAME)
            .description(UPDATED_DESCRIPTION)
            .active(UPDATED_ACTIVE)
            .mddIds(updatedHeading.getMdds().stream().map(Mdd::getId).collect(Collectors.toSet()))
            .build();

        restHeadingMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(headingReq)))
            .andExpect(status().isOk());

        // Validate the Heading in the database
        List<Heading> headingList = headingRepository.findAll();
        assertThat(headingList).hasSize(databaseSizeBeforeUpdate);
        Heading testHeading = headingList.get(headingList.size() - 1);
        assertThat(testHeading.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testHeading.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testHeading.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingHeading() throws Exception {
        int databaseSizeBeforeUpdate = headingRepository.findAll().size();
        headingReq.setId(count.incrementAndGet());

        restHeadingMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(headingReq)))
            .andExpect(status().isNotFound());

        // Validate the Heading in the database
        List<Heading> headingList = headingRepository.findAll();
        assertThat(headingList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteHeading() throws Exception {
        // Initialize the database
        headingRepository.saveAndFlush(heading);

        int databaseSizeBeforeDelete = headingRepository.findAll().size();

        // Delete the heading
        restHeadingMockMvc
            .perform(delete(ENTITY_API_URL_ID, heading.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Heading> headingList = headingRepository.findAll();
        assertThat(headingList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getHeading() throws Exception {
        // Initialize the database
        headingRepository.saveAndFlush(heading);

        // Get the heading
        restHeadingMockMvc
            .perform(get(ENTITY_API_URL_ID, heading.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(heading.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingHeading() throws Exception {
        // Get the heading
        restHeadingMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getAllHeadings() throws Exception {
        // Initialize the database
        headingRepository.saveAndFlush(heading);

        // Get all the headingList
        restHeadingMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(heading.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }

//    @Test
//    @Transactional
//    @SuppressWarnings({"unchecked"})
//    void getAllHeadingsWithEagerRelationshipsIsEnabled() throws Exception {
//        when(headingRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
//
//        restHeadingMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());
//
//        verify(headingRepositoryMock, times(1)).findAllWithEagerRelationships(any());
//    }
//
//    @Test
//    @Transactional
//    @SuppressWarnings({"unchecked"})
//    void getAllHeadingsWithEagerRelationshipsIsNotEnabled() throws Exception {
//        when(headingRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
//
//        restHeadingMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());
//
//        verify(headingRepositoryMock, times(1)).findAllWithEagerRelationships(any());
//    }
}
