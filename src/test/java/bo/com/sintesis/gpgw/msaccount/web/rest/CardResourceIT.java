package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Card;
import bo.com.bisa.gpgw.domain.Customer;
import bo.com.bisa.gpgw.domain.PgwMid;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.web.rest.request.CardReq;
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
class CardResourceIT {

    private static final String DEFAULT_INSTRUMENT_ID = "AAAAAAAAAA";
    private static final String UPDATED_INSTRUMENT_ID = "BBBBBBBBBB";

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/cards";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong count = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCardMockMvc;

    private Card card;

    private CardReq cardReq;

    public static Card createEntity(EntityManager em) {
        Card card = Card.builder()
            .instrumentIdentifierId(DEFAULT_INSTRUMENT_ID)
//            .isDefault(DEFAULT_ACTIVE)
            .build();

        // Add required entity
        Customer customer;
        List<Customer> customers = TestUtil.findAll(em, Customer.class);
        if (customers.isEmpty()) {
            List<PgwMid> pgwMids = TestUtil.findAll(em, PgwMid.class);
            customer = Customer.builder()
//                .customerUserId("Customer user id test")
//                .firstName("Customer firstName test")
//                .lastName("Customer lastName test")
//                .address1("DEFAULT_ADDRESS_1")
//                .address2("DEFAULT_ADDRESS_2")
//                .locality("DEFAULT_LOCALITY")
//                .administrativeArea("DEFAULT_ADMINISTRATIVE_AREA")
//                .postalCode("DEFAULT_POSTAL_CODE")
//                .country("DEFAULT_COUNTRY")
//                .email("Customer email test")
//                .phoneNumber("Customer cellPhone test")
//                .customerTokenId("Customer token test")
//                .pgwMid(pgwMids.get(0))
                .build();
            em.persist(customer);
            em.flush();
        } else {
            customer = customers.get(0);
        }
        card.setCustomer(customer);

        // Add required entity
//        CardType cardType;
//        List<CardType> cardTypes = TestUtil.findAll(em, CardType.class);
//        if (cardTypes.isEmpty()) {
//            cardType = CardType.builder()
//                .code("CardType code test")
//                .name("CardType name test")
//                .description("CardType description test")
//                .build();
//            em.persist(cardType);
//            em.flush();
//        } else {
//            cardType = cardTypes.get(0);
//        }
//        card.setCardType(cardType);
        return card;
    }

    public static CardReq createRequest(Card card) {
        return CardReq.builder()
            .instrumentId(card.getInstrumentIdentifierId())
//            .active(card.getIsDefault())
            .customerId(card.getCustomer().getId())
//            .cardTypeId(card.getCardType().getId())
            .build();
    }

    @BeforeEach
    public void initTest() {
        card = createEntity(em);
        cardReq = createRequest(card);
    }

    @Test
    @Transactional
    void createCard() throws Exception {
        int databaseSizeBeforeCreate = cardRepository.findAll().size();
        // Create the Card
        restCardMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(cardReq)))
            .andExpect(status().isCreated());

        // Validate the Card in the database
        List<Card> cardList = cardRepository.findAll();
        assertThat(cardList).hasSize(databaseSizeBeforeCreate + 1);
        Card testCard = cardList.get(cardList.size() - 1);
        assertThat(testCard.getInstrumentIdentifierId()).isEqualTo(DEFAULT_INSTRUMENT_ID);
//        assertThat(testCard.getIsDefault()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    void checkPaymentInstrumentIdIsRequired() throws Exception {
        int databaseSizeBeforeTest = cardRepository.findAll().size();
        // set the field null
        cardReq.setInstrumentId(null);

        restCardMockMvc
            .perform(post(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(card)))
            .andExpect(status().isBadRequest());

        List<Card> cardList = cardRepository.findAll();
        assertThat(cardList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void putNewCard() throws Exception {
        // Initialize the database
        Card updateCard = cardRepository.saveAndFlush(card);

        int databaseSizeBeforeUpdate = cardRepository.findAll().size();

        CardReq cardReq = CardReq.builder()
            .id(updateCard.getId())
            .instrumentId(UPDATED_INSTRUMENT_ID)
            .active(UPDATED_ACTIVE)
            .customerId(updateCard.getCustomer().getId())
//            .cardTypeId(updateCard.getCardType().getId())
            .build();

        restCardMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(cardReq)))
            .andExpect(status().isOk());

        // Validate the Card in the database
        List<Card> cardList = cardRepository.findAll();
        assertThat(cardList).hasSize(databaseSizeBeforeUpdate);
        Card testCard = cardList.get(cardList.size() - 1);
        assertThat(testCard.getInstrumentIdentifierId()).isEqualTo(UPDATED_INSTRUMENT_ID);
//        assertThat(testCard.getIsDefault()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    void putNonExistingCard() throws Exception {
        int databaseSizeBeforeUpdate = cardRepository.findAll().size();
        cardReq.setId(count.incrementAndGet());

        restCardMockMvc
            .perform(put(ENTITY_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(cardReq)))
            .andExpect(status().isNotFound());

        // Validate the Card in the database
        List<Card> cardList = cardRepository.findAll();
        assertThat(cardList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCard() throws Exception {
        // Initialize the database
        cardRepository.saveAndFlush(card);

        int databaseSizeBeforeDelete = cardRepository.findAll().size();

        // Delete the card
        restCardMockMvc
            .perform(delete(ENTITY_API_URL_ID, card.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Card> cardList = cardRepository.findAll();
        assertThat(cardList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    void getCard() throws Exception {
        // Initialize the database
        cardRepository.saveAndFlush(card);

        // Get the card
        restCardMockMvc
            .perform(get(ENTITY_API_URL_ID, card.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(card.getId().intValue()))
            .andExpect(jsonPath("$.instrumentId").value(DEFAULT_INSTRUMENT_ID))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE));
    }

    @Test
    @Transactional
    void getNonExistingCard() throws Exception {
        // Get the card
        restCardMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getAllCards() throws Exception {
        // Initialize the database
        cardRepository.saveAndFlush(card);

        // Get all the cardList
        restCardMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(card.getId().intValue())))
            .andExpect(jsonPath("$.[*].instrumentId").value(hasItem(DEFAULT_INSTRUMENT_ID)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)));
    }
}
