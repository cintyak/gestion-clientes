package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.PaymentGateway;
import bo.com.bisa.gpgw.domain.PgwTigoMoney;
import bo.com.bisa.gpgw.domain.TigoMoneyConfig;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.PgwTigoMoneyRepository;
import bo.com.bisa.gpgw.msaccount.service.PgwTigoMoneyService;
import bo.com.bisa.gpgw.msaccount.service.dto.PgwTigoMoneyDTO;
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
class PgwTigoMoneyResourceIT {

    private static final String ENTITY_API_URL = "/api/pgw-tigo-monies";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private PgwTigoMoneyRepository pgwTigoMoneyRepository;

    @Autowired
    private PgwTigoMoneyService pgwTigoMoneyService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPgwTigoMoneyMockMvc;

    private PgwTigoMoney pgwTigoMoney;

    public static PgwTigoMoney createEntity(EntityManager em) {
        PgwTigoMoney pgwTigoMoney = new PgwTigoMoney();
        // Add required entity
        PaymentGateway paymentGateway;
        if (TestUtil.findAll(em, PaymentGateway.class).isEmpty()) {
            paymentGateway = PaymentGatewayResourceIT.createEntity(em);
            em.persist(paymentGateway);
            em.flush();
        } else {
            paymentGateway = TestUtil.findAll(em, PaymentGateway.class).get(0);
        }
        pgwTigoMoney.setPaymentGateway(paymentGateway);
        // Add required entity
        TigoMoneyConfig tigoMoneyConfig;
        if (TestUtil.findAll(em, TigoMoneyConfig.class).isEmpty()) {
            tigoMoneyConfig = TigoMoneyConfigResourceIT.createEntity();
            em.persist(tigoMoneyConfig);
            em.flush();
        } else {
            tigoMoneyConfig = TestUtil.findAll(em, TigoMoneyConfig.class).get(0);
        }
        pgwTigoMoney.setTigoMoneyConfig(tigoMoneyConfig);
        return pgwTigoMoney;
    }

    @BeforeEach
    public void initTest() {
        pgwTigoMoney = createEntity(em);
    }

    @Test
    @Transactional
    void createPgwTigoMoney() throws Exception {
        int databaseSizeBeforeCreate = pgwTigoMoneyRepository.findAll().size();
        // Create the PgwTigoMoney
        PgwTigoMoneyDTO pgwTigoMoneyDTO = pgwTigoMoneyService.toDto(pgwTigoMoney);
        restPgwTigoMoneyMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(pgwTigoMoneyDTO))
            )
            .andExpect(status().isCreated());

        // Validate the PgwTigoMoney in the database
        List<PgwTigoMoney> pgwTigoMoneyList = pgwTigoMoneyRepository.findAll();
        assertThat(pgwTigoMoneyList).hasSize(databaseSizeBeforeCreate + 1);
        PgwTigoMoney testPgwTigoMoney = pgwTigoMoneyList.get(pgwTigoMoneyList.size() - 1);
    }

    @Test
    @Transactional
    void getAllPgwTigoMonies() throws Exception {
        // Initialize the database
        pgwTigoMoneyRepository.saveAndFlush(pgwTigoMoney);

        // Get all the pgwTigoMoneyList
        restPgwTigoMoneyMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pgwTigoMoney.getId().intValue())));
    }

    @Test
    @Transactional
    void getPgwTigoMoney() throws Exception {
        // Initialize the database
        pgwTigoMoneyRepository.saveAndFlush(pgwTigoMoney);

        // Get the pgwTigoMoney
        restPgwTigoMoneyMockMvc
            .perform(get(ENTITY_API_URL_ID, pgwTigoMoney.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(pgwTigoMoney.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingPgwTigoMoney() throws Exception {
        // Get the pgwTigoMoney
        restPgwTigoMoneyMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPgwTigoMoney() throws Exception {
        // Initialize the database
        PgwTigoMoney updatedPgwTigoMoney = pgwTigoMoneyRepository.saveAndFlush(pgwTigoMoney);

        int databaseSizeBeforeUpdate = pgwTigoMoneyRepository.findAll().size();

        PgwTigoMoneyDTO pgwTigoMoneyDTO = pgwTigoMoneyService.toDto(updatedPgwTigoMoney);

        restPgwTigoMoneyMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pgwTigoMoneyDTO))
            )
            .andExpect(status().isOk());

        // Validate the PgwTigoMoney in the database
        List<PgwTigoMoney> pgwTigoMoneyList = pgwTigoMoneyRepository.findAll();
        assertThat(pgwTigoMoneyList).hasSize(databaseSizeBeforeUpdate);
        PgwTigoMoney testPgwTigoMoney = pgwTigoMoneyList.get(pgwTigoMoneyList.size() - 1);
    }

    @Test
    @Transactional
    void putNonExistingPgwTigoMoney() throws Exception {
        int databaseSizeBeforeUpdate = pgwTigoMoneyRepository.findAll().size();
        pgwTigoMoney.setId(count.incrementAndGet());

        // Create the PgwTigoMoney
        PgwTigoMoneyDTO pgwTigoMoneyDTO = pgwTigoMoneyService.toDto(pgwTigoMoney);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPgwTigoMoneyMockMvc
            .perform(
                put(ENTITY_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(pgwTigoMoneyDTO))
            )
            .andExpect(status().isNotFound());

        // Validate the PgwTigoMoney in the database
        List<PgwTigoMoney> pgwTigoMoneyList = pgwTigoMoneyRepository.findAll();
        assertThat(pgwTigoMoneyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePgwTigoMoney() throws Exception {
        // Initialize the database
        pgwTigoMoneyRepository.saveAndFlush(pgwTigoMoney);

        int databaseSizeBeforeDelete = pgwTigoMoneyRepository.findAll().size();

        // Delete the pgwTigoMoney
        restPgwTigoMoneyMockMvc
            .perform(delete(ENTITY_API_URL_ID, pgwTigoMoney.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<PgwTigoMoney> pgwTigoMoneyList = pgwTigoMoneyRepository.findAll();
        assertThat(pgwTigoMoneyList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
