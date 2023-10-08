package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Mid;
import bo.com.bisa.gpgw.domain.PaymentGateway;
import bo.com.bisa.gpgw.domain.PgwMid;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.PgwMidRepository;
import bo.com.bisa.gpgw.msaccount.web.rest.request.PgwMidReq;
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
class PgwMidResourceIT {

    private static final String ENTITY_API_URL = "/api/pgw-mids";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private PgwMidRepository pgwMidRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPgwMidMockMvc;

    private PgwMid pgwMid;

    private PgwMidReq pgwMidReq;

    public static PgwMid createEntity(EntityManager em) {
        PgwMid pgwMid = PgwMid.builder()
            .build();

        // Add required entity
        PaymentGateway paymentGateway = TestUtil.findAll(em, PaymentGateway.class).get(0);
        pgwMid.setPaymentGateway(paymentGateway);

        // Add required entity
        Mid mid = TestUtil.findAll(em, Mid.class).get(0);
        pgwMid.setMid(mid);

        return pgwMid;
    }

    public static PgwMidReq createRequest(PgwMid pgwMid) {
        return PgwMidReq.builder()
            .paymentGatewayId(pgwMid.getPaymentGateway().getId())
            .midId(pgwMid.getMid().getId())
            .build();
    }

    @BeforeEach
    public void initTest() {
        pgwMid = createEntity(em);
        pgwMidReq = createRequest(pgwMid);
    }

    @Test
    @Transactional
    void createPgwMid() throws Exception {
        int databaseSizeBeforeCreate = pgwMidRepository.findAll().size();
        // Create the PgwMid
        restPgwMidMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(pgwMidReq)))
            .andExpect(status().isCreated());

        // Validate the PgwMid in the database
        List<PgwMid> pgwMidList = pgwMidRepository.findAll();
        assertThat(pgwMidList).hasSize(databaseSizeBeforeCreate + 1);
        PgwMid testPgwMid = pgwMidList.get(pgwMidList.size() - 1);
    }

    @Test
    @Transactional
    void checkActiveIsRequired() throws Exception {
        int databaseSizeBeforeTest = pgwMidRepository.findAll().size();
        // set the field null
        pgwMidReq.setActive(null);

        restPgwMidMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(pgwMidReq)))
            .andExpect(status().isBadRequest());

        List<PgwMid> pgwMidList = pgwMidRepository.findAll();
        assertThat(pgwMidList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void putNewPgwMid() throws Exception {
        // Initialize the database
        PgwMid updatedPgwMid = pgwMidRepository.saveAndFlush(pgwMid);

        int databaseSizeBeforeUpdate = pgwMidRepository.findAll().size();

        PgwMidReq pgwMidReq = PgwMidReq.builder()
            .id(updatedPgwMid.getId())
            .paymentGatewayId(updatedPgwMid.getPaymentGateway().getId())
            .midId(updatedPgwMid.getMid().getId())
            .build();

        restPgwMidMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(pgwMidReq)))
            .andExpect(status().isOk());

        // Validate the PgwMid in the database
        List<PgwMid> pgwMidList = pgwMidRepository.findAll();
        assertThat(pgwMidList).hasSize(databaseSizeBeforeUpdate);
        PgwMid testPgwMid = pgwMidList.get(pgwMidList.size() - 1);
    }

    @Test
    @Transactional
    void putNonExistingPgwMid() throws Exception {
        int databaseSizeBeforeUpdate = pgwMidRepository.findAll().size();
        pgwMidReq.setId(count.incrementAndGet());

        restPgwMidMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(pgwMidReq)))
            .andExpect(status().isNotFound());

        // Validate the PgwMid in the database
        List<PgwMid> pgwMidList = pgwMidRepository.findAll();
        assertThat(pgwMidList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePgwMid() throws Exception {
        // Initialize the database
        pgwMidRepository.saveAndFlush(pgwMid);

        int databaseSizeBeforeDelete = pgwMidRepository.findAll().size();

        // Delete the pgwMid
        restPgwMidMockMvc
            .perform(delete(ENTITY_API_URL_ID, pgwMid.getId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PgwMid> pgwMidList = pgwMidRepository.findAll();
        assertThat(pgwMidList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getPgwMid() throws Exception {
        // Initialize the database
        pgwMidRepository.saveAndFlush(pgwMid);

        // Get the pgwMid
        restPgwMidMockMvc
            .perform(get(ENTITY_API_URL_ID, pgwMid.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pgwMid.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingPgwMid() throws Exception {
        // Get the pgwMid
        restPgwMidMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getAllPgwMids() throws Exception {
        // Initialize the database
        pgwMidRepository.saveAndFlush(pgwMid);

        // Get all the pgwMidList
        restPgwMidMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pgwMid.getId().intValue())));
    }
}
