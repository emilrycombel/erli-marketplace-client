package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.model.offer.CreateOfferRequest;
import pl.er.shoper.client.model.offer.OfferPriceUpdate;
import pl.er.shoper.client.model.offer.OfferStockUpdate;
import pl.er.shoper.client.model.offer.UpdateOfferRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OfferApiTest {

    private MockRestServiceServer server;
    private OfferApi offerApi;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://shop.example.com");
        server = MockRestServiceServer.bindTo(builder).build();
        offerApi = new OfferApi(builder.build(), new ObjectMapper());
    }

    @Test
    void findByIdReturnsOffer() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/offers/10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"offer_id": 10, "name": "Test Offer", "price": 29.99, "stock": 50, "active": 1}
                        """, MediaType.APPLICATION_JSON));

        var offer = offerApi.findById(10);

        assertEquals(10L, offer.offerId());
        assertEquals("Test Offer", offer.name());
        assertEquals(new BigDecimal("29.99"), offer.price());
        server.verify();
    }

    @Test
    void createSendsPostRequest() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/offers"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"offer_id": 20, "name": "New Offer", "price": 15.00}
                        """, MediaType.APPLICATION_JSON));

        var request = new CreateOfferRequest(1L, "New Offer", "1234567890123", new BigDecimal("15.00"), 100);
        var offer = offerApi.create(request);

        assertEquals(20L, offer.offerId());
        server.verify();
    }

    @Test
    void updateSendsPutRequest() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/offers/10"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("""
                        {"offer_id": 10, "name": "Updated Offer"}
                        """, MediaType.APPLICATION_JSON));

        var request = new UpdateOfferRequest("Updated Offer", null, null, null, null);
        var offer = offerApi.update(10, request);

        assertEquals("Updated Offer", offer.name());
        server.verify();
    }

    @Test
    void updateStockSendsPutRequest() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/offers/10"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        offerApi.updateStock(10, new OfferStockUpdate(200));
        server.verify();
    }

    @Test
    void updatePriceSendsPutRequest() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/offers/10"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        offerApi.updatePrice(10, new OfferPriceUpdate(new BigDecimal("49.99")));
        server.verify();
    }

    @Test
    void deactivateSendsPutWithActiveFalse() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/offers/10"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        offerApi.deactivate(10);
        server.verify();
    }
}
