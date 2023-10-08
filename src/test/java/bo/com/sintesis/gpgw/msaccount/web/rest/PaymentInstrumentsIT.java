package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Customer;
import bo.com.bisa.gpgw.domain.PgwMid;
import bo.com.bisa.gpgw.msaccount.IntegrationTest;
import bo.com.bisa.gpgw.msaccount.repository.CustomerRepository;
import bo.com.bisa.gpgw.msaccount.security.AuthoritiesConstants;
import bo.com.bisa.gpgw.msaccount.service.PgwMidService;
import bo.com.bisa.gpgw.msaccount.web.rest.request.PaymentInstrumentCreateReq;
import bo.com.bisa.gpgw.msaccount.web.rest.util.TestUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.USER)
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentInstrumentsIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PgwMidService pgwMidService;

    private String instrumentId;
    private String token;
    private String userPaymentId;
    private Long paymentGatewayId;
    private String cardNumber;

    @BeforeAll
    public void setUp() {
        this.token = "CD14E3A9CE527C0EE053A2598D0A811D";
        this.userPaymentId = "1234";
        this.paymentGatewayId = 1L;
        this.cardNumber = "5555555555554444";
        PaymentInstrumentCreateReq req = new PaymentInstrumentCreateReq();
        req.setName("John");
        req.setLastname("Doe");
        req.setAddress("1 Market St");
        req.setCity("san francisco");
        req.setState("CA");
        req.setPostalCode("94105");
        req.setCountry("US");
        req.setEmail("test@cybs.com");
        req.setPhone("4158880000");
        Optional<PgwMid> midOptional = pgwMidService.getByPaymentGatewayId(paymentGatewayId);

        Customer c = new Customer();
//        c.setCustomerUserId(userPaymentId);
//        c.setFirstName(req.getName());
//        c.setLastName(req.getLastname());
//        c.setEmail(req.getEmail());
//        c.setPhoneNumber(req.getPhone());
//        c.setCustomerTokenId(token);
//        c.setPgwMid(midOptional.get());
        customerRepository.save(c);
    }

    //@Test
    @Order(1)
    void testPostTokenList() throws Exception {
        /*
        para el tipo de tarjeta
        001 : visa
        002 : mastercard - Eurocard—European regional brand of Mastercard
        003 : american express
        ...
        */
        PaymentInstrumentCreateReq req = new PaymentInstrumentCreateReq();
        req.setUserPaymentId(userPaymentId);
        req.setPaymentGatewayId(paymentGatewayId);
        req.setCardNumber(cardNumber);
        req.setCardExpirationMonth("12");
        req.setCardExpirationYear("2031");
        req.setSecureCode("123");
        req.setCardType("002");

        req.setName("John");
        req.setLastname("Doe");
        req.setAddress("1 Market St");
        req.setCity("san francisco");
        req.setState("CA");
        req.setPostalCode("94105");
        req.setCountry("US");
        req.setEmail("test@cybs.com");
        req.setPhone("4158880000");
        req.setDefaultPaymentInstrument(true);

        mockMvc
            .perform(post("/api/customer/create").contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(req)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().string(""));
    }

    @Test
    @Order(2)
    void testGetTokenList() throws Exception {

        MvcResult result = mockMvc
            .perform(get("/api/customer/{userPaymentId}/{paymentGatewayId}/paymentInstruments", userPaymentId, paymentGatewayId))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$").isArray())
//            .andExpect(jsonPath("$[0].email", Matchers.equalTo("test@cybs.com")))
            .andReturn();
//        instrumentId = JsonPath.read(result.getResponse().getContentAsString(), "$[0].paymentInstrumentTokenId");
    }

    //@Test
    @Order(3)
    void testDeleteToken() throws Exception {

        mockMvc
            .perform(delete("/api/customer/delete/{userPaymentId}/{paymentGatewayId}/{instrumentId}", userPaymentId, paymentGatewayId, instrumentId))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().string(""));
    }
}
