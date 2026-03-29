package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.filter.FilterBuilder;
import pl.er.shoper.client.model.product.CreateProductRequest;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ProductApiTest {

    private MockRestServiceServer server;
    private ProductApi productApi;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://shop.example.com");
        server = MockRestServiceServer.bindTo(builder).build();
        productApi = new ProductApi(builder.build(), new ObjectMapper());
    }

    @Test
    void findByIdReturnsProduct() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/products/42"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"product_id": 42, "code": "SKU-001", "ean": "1234567890123"}
                        """, MediaType.APPLICATION_JSON));

        var product = productApi.findById(42);

        assertEquals(42L, product.productId());
        assertEquals("SKU-001", product.code());
        assertEquals("1234567890123", product.ean());
        server.verify();
    }

    @Test
    void createSendsPostRequest() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/products"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"product_id": 99, "code": "NEW-001", "ean": "9876543210123"}
                        """, MediaType.APPLICATION_JSON));

        var request = new CreateProductRequest(
                "NEW-001", "9876543210123", 1L, 5L, 1L, 1L,
                new CreateProductRequest.StockData(new BigDecimal("19.99"), new BigDecimal("100"), null),
                Map.of("pl_PL", new CreateProductRequest.TranslationData("Test Product", "Short", "Long", 1))
        );

        var product = productApi.create(request);

        assertEquals(99L, product.productId());
        server.verify();
    }

    @Test
    void findAllWithFiltersUsesCorrectUrl() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/products?page=1&filters%5Bcategory_id%5D=5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"count": 1, "pages": 1, "page": 1, "list": [{"product_id": 1, "code": "A"}]}
                        """, MediaType.APPLICATION_JSON));

        var products = productApi.findAll(FilterBuilder.create().eq("category_id", 5)).toList();

        assertEquals(1, products.size());
        assertEquals(1L, products.getFirst().productId());
        server.verify();
    }
}
