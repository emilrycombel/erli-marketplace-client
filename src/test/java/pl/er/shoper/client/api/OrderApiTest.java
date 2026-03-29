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

class OrderApiTest {

    private MockRestServiceServer server;
    private OrderApi orderApi;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://shop.example.com");
        server = MockRestServiceServer.bindTo(builder).build();
        orderApi = new OrderApi(builder.build(), new ObjectMapper());
    }

    @Test
    void findByIdReturnsOrder() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/orders/100"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"order_id": 100, "status_id": 2, "sum": 199.99, "email": "test@example.com", "date": "2025-01-15"}
                        """, MediaType.APPLICATION_JSON));

        var order = orderApi.findById(100);

        assertEquals(100L, order.orderId());
        assertEquals(2, order.statusId());
        assertEquals(new BigDecimal("199.99"), order.sum());
        assertEquals("test@example.com", order.email());
        server.verify();
    }

    @Test
    void findAllReturnsPaginatedOrders() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/orders?page=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"count": 2, "pages": 1, "page": 1, "list": [
                            {"order_id": 1, "status_id": 1, "sum": 50.00},
                            {"order_id": 2, "status_id": 2, "sum": 75.00}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        var orders = orderApi.findAll().toList();

        assertEquals(2, orders.size());
        assertEquals(1L, orders.getFirst().orderId());
        server.verify();
    }
}
