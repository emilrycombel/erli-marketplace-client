package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CompetitionApiTest {

    private MockRestServiceServer server;
    private CompetitionApi competitionApi;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://shop.example.com");
        server = MockRestServiceServer.bindTo(builder).build();
        competitionApi = new CompetitionApi(builder.build(), new ObjectMapper());
    }

    @Test
    void findByEanReturnsCompetitionOffers() {
        server.expect(requestTo(
                        "https://shop.example.com/webapi/rest/competition-offers?page=1&filters%5Bean%5D=1234567890123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"count": 2, "pages": 1, "page": 1, "list": [
                            {"offer_id": 1, "seller": "Shop A", "ean": "1234567890123", "price": 29.99},
                            {"offer_id": 2, "seller": "Shop B", "ean": "1234567890123", "price": 34.99}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        var offers = competitionApi.findByEan("1234567890123").toList();

        assertEquals(2, offers.size());
        assertEquals("Shop A", offers.getFirst().seller());
        assertEquals(new BigDecimal("29.99"), offers.getFirst().price());
        server.verify();
    }
}
