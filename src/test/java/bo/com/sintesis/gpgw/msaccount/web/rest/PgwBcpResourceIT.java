package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.BcpConfig;
import bo.com.bisa.gpgw.domain.PaymentGateway;
import bo.com.bisa.gpgw.domain.PgwBcp;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.PgwBcpRepository;
import bo.com.bisa.gpgw.msaccount.service.PgwBcpService;
import bo.com.bisa.gpgw.msaccount.service.dto.PgwBcpDTO;
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
class PgwBcpResourceIT {

    private static final String ENTITY_API_URL = "/api/pgw-bcps";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private PgwBcpRepository pgwBcpRepository;

    @Autowired
    private PgwBcpService pgwBcpService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPgwBcpMockMvc;

    private PgwBcp pgwBcp;

    public static PgwBcp createEntity(EntityManager em) {
        PgwBcp pgwBcp = new PgwBcp();
        // Add required entity
        PaymentGateway paymentGateway;
        if (TestUtil.findAll(em, PaymentGateway.class).isEmpty()) {
            paymentGateway = PaymentGatewayResourceIT.createEntity(em);
            em.persist(paymentGateway);
            em.flush();
        } else {
            paymentGateway = TestUtil.findAll(em, PaymentGateway.class).get(0);
        }
        pgwBcp.setPaymentGateway(paymentGateway);
        // Add required entity
        BcpConfig bcpConfig;
        if (TestUtil.findAll(em, BcpConfig.class).isEmpty()) {
            bcpConfig = BcpConfigResourceIT.createEntity();
            em.persist(bcpConfig);
            em.flush();
        } else {
            bcpConfig = TestUtil.findAll(em, BcpConfig.class).get(0);
        }
        pgwBcp.setBcpConfig(bcpConfig);
        return pgwBcp;
    }

    @BeforeEach
    public void initTest() {
        pgwBcp = createEntity(em);
    }

    @Test
    @Transactional
    void createPgwBcp() throws Exception {
        int databaseSizeBeforeCreate = pgwBcpRepository.findAll().size();
        // Create the PgwBcp
        PgwBcpDTO pgwBcpDTO = pgwBcpService.toDto(pgwBcp);
        restPgwBcpMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pgwBcpDTO)))
            .andExpect(status().isCreated());

        // Validate the PgwBcp in the database
        List<PgwBcp> pgwBcpList = pgwBcpRepository.findAll();
        assertThat(pgwBcpList).hasSize(databaseSizeBeforeCreate + 1);
    }

    @Test
    @Transactional
    void getAllPgwBcps() throws Exception {
        // Initialize the database
        pgwBcpRepository.saveAndFlush(pgwBcp);

        // Get all the pgwBcpList
        restPgwBcpMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pgwBcp.getId().intValue())));
    }

    @Test
    @Transactional
    void getPgwBcp() throws Exception {
        // Initialize the database
        pgwBcpRepository.saveAndFlush(pgwBcp);

        // Get the pgwBcp
        restPgwBcpMockMvc
            .perform(get(ENTITY_API_URL_ID, pgwBcp.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pgwBcp.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingPgwBcp() throws Exception {
        // Get the pgwBcp
        restPgwBcpMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPgwBcp() throws Exception {
        // Initialize the database
        PgwBcp updatedPgwBcp = pgwBcpRepository.saveAndFlush(pgwBcp);

        int databaseSizeBeforeUpdate = pgwBcpRepository.findAll().size();

        PgwBcpDTO pgwBcpDTO = pgwBcpService.toDto(updatedPgwBcp);

        restPgwBcpMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pgwBcpDTO))
            )
            .andExpect(status().isOk());

        // Validate the PgwBcp in the database
        List<PgwBcp> pgwBcpList = pgwBcpRepository.findAll();
        assertThat(pgwBcpList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putNonExistingPgwBcp() throws Exception {
        int databaseSizeBeforeUpdate = pgwBcpRepository.findAll().size();
        pgwBcp.setId(count.incrementAndGet());

        // Create the PgwBcp
        PgwBcpDTO pgwBcpDTO = pgwBcpService.toDto(pgwBcp);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPgwBcpMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pgwBcpDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the PgwBcp in the database
        List<PgwBcp> pgwBcpList = pgwBcpRepository.findAll();
        assertThat(pgwBcpList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePgwBcp() throws Exception {
        // Initialize the database
        pgwBcpRepository.saveAndFlush(pgwBcp);

        int databaseSizeBeforeDelete = pgwBcpRepository.findAll().size();

        // Delete the pgwBcp
        restPgwBcpMockMvc
            .perform(delete(ENTITY_API_URL_ID, pgwBcp.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PgwBcp> pgwBcpList = pgwBcpRepository.findAll();
        assertThat(pgwBcpList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
