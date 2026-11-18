package pl.erli.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.erli.client.exception.ErliApiException;
import pl.erli.client.model.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.web.client.ResourceAccessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

class ErliClientHttpTest {

    record Recorded(String method, String path, String query, String auth, String body) {}

    record Reply(int status, String body) {}

    private HttpServer server;
    private final List<Recorded> requests = new ArrayList<>();
    private final Queue<Reply> replies = new ConcurrentLinkedQueue<>();
    private ErliClient client;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        client = new ErliClientBuilder()
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/svc/shop-api")
                .token("test-key")
                .initialDelayMs(10)
                .pageSize(2)
                .build();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private synchronized void handle(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        requests.add(new Recorded(ex.getRequestMethod(), ex.getRequestURI().getPath(),
                ex.getRequestURI().getQuery(), ex.getRequestHeaders().getFirst("Authorization"), body));
        Reply reply = replies.poll();
        if (reply == null) reply = new Reply(500, "{\"message\":\"no reply queued\"}");
        byte[] bytes = reply.body().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(reply.status(), bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) ex.getResponseBody().write(bytes);
        ex.close();
    }

    @Test
    void sendsBearerTokenAndParsesShop() {
        replies.add(new Reply(200, """
                {"id":4,"name":"my-shop","active":true,"company":{"nip":"1234567890","name":"ACME"},
                 "externalMatchingPolicy":"enabled"}"""));

        ShopResponse shop = client.getShopInfo();

        assertEquals("my-shop", shop.name());
        assertEquals("ACME", shop.company().get("name"));
        Recorded r = requests.getFirst();
        assertEquals("GET", r.method());
        assertEquals("/svc/shop-api/me", r.path());
        assertEquals("Bearer test-key", r.auth());
    }

    @Test
    void createProductPostsRequiredFieldsAndOmitsNulls() {
        replies.add(new Reply(202, ""));
        var product = ProductCreate.builder("Kurtka zimowa",
                        List.of(new ProductImage("https://example.com/a.jpg", null, null, null)),
                        10000, 10, new DispatchTime(null, 1))
                .ean("5901234123457")
                .description(ProductDescription.of(ProductDescription.section(
                        ProductDescription.Item.text("<h1>Kurtka</h1>"))))
                .build();

        client.createProduct("sku-1", product);

        Recorded r = requests.getFirst();
        assertEquals("POST", r.method());
        assertEquals("/svc/shop-api/products/sku-1", r.path());
        assertTrue(r.body().contains("\"name\":\"Kurtka zimowa\""), r.body());
        assertTrue(r.body().contains("\"price\":10000"), r.body());
        assertTrue(r.body().contains("\"dispatchTime\":{\"period\":1}"), r.body());
        assertTrue(r.body().contains("\"sections\":[{\"items\":[{\"type\":\"TEXT\",\"content\":\"<h1>Kurtka</h1>\"}]}]"), r.body());
        assertFalse(r.body().contains("null"), "nulls must be omitted: " + r.body());
    }

    @Test
    void createProductConflictRaisesApiException() {
        replies.add(new Reply(409, "{\"message\":\"exists\"}"));
        var product = ProductCreate.builder("Kurtka zimowa",
                List.of(new ProductImage("https://example.com/a.jpg", null, null, null)),
                10000, 10, new DispatchTime(null, 1)).build();

        var ex = assertThrows(ErliApiException.class, () -> client.createProduct("sku-1", product));
        assertEquals(409, ex.statusCode());
    }

    @Test
    void stockPriceAndDeactivateSendOnlyThatField() {
        replies.add(new Reply(202, "{\"updatedFields\":[\"stock\"]}"));
        replies.add(new Reply(202, "{\"updatedFields\":[\"price\"]}"));
        replies.add(new Reply(202, "{\"updatedFields\":[\"status\"]}"));

        assertEquals(List.of("stock"), client.updateStock("sku-1", 5).updatedFields());
        client.updatePrice("sku-1", 4999);
        client.deactivateProduct("sku-1");

        assertEquals("PATCH", requests.get(0).method());
        assertEquals("{\"stock\":5}", requests.get(0).body());
        assertEquals("{\"price\":4999}", requests.get(1).body());
        assertEquals("{\"status\":\"inactive\"}", requests.get(2).body());
    }

    @Test
    void productSearchSerializesFilterAndPaginatesByExternalId() {
        replies.add(new Reply(200, "[" + product("a") + "," + product("b") + "]"));
        replies.add(new Reply(200, "[" + product("c") + "]"));
        replies.add(new Reply(200, "[]"));

        List<String> ids = client.searchProducts(new ProductSearchRequest(null,
                        Filter.and(Filter.eq("status", "active"), Filter.gt("stock", 0)), null))
                .map(Product::externalId).toList();

        assertEquals(List.of("a", "b", "c"), ids);
        assertTrue(requests.get(0).body().contains(
                "\"filter\":{\"operator\":\"and\",\"value\":[{\"field\":\"status\",\"operator\":\"=\",\"value\":\"active\"},"
                        + "{\"field\":\"stock\",\"operator\":\">\",\"value\":0}]}"), requests.get(0).body());
        assertFalse(requests.get(0).body().contains("\"after\""), requests.get(0).body());
        assertTrue(requests.get(1).body().contains("\"after\":\"b\""), requests.get(1).body());
    }

    @Test
    void orderSearchUsesCursorFromLastOrder() {
        replies.add(new Reply(200, "[" + order("1", "100;1x1") + "," + order("2", "100;2x2") + "]"));
        replies.add(new Reply(200, "[]"));

        List<Order> orders = client.searchOrders(new OrderSearchRequest(null, null)).toList();

        assertEquals(2, orders.size());
        assertEquals("purchased", orders.getFirst().status());
        assertEquals(1, orders.getFirst().items().size());
        assertTrue(requests.get(0).body().contains("\"sortField\":\"updated\""), requests.get(0).body());
        assertTrue(requests.get(1).body().contains("\"after\":\"100;2x2\""), requests.get(1).body());
    }

    @Test
    void searchesStartFromTheCallersCursor() {
        replies.add(new Reply(200, "[]"));
        replies.add(new Reply(200, "[]"));

        client.searchOrders(new OrderSearchRequest(
                new Pagination("updated", "100;5x5", SortOrder.ASC, null), null)).toList();
        client.searchProducts(new ProductSearchRequest(
                new Pagination("externalId", "sku-9", SortOrder.ASC, null), null, null)).toList();

        assertTrue(requests.get(0).body().contains("\"after\":\"100;5x5\""), requests.get(0).body());
        assertTrue(requests.get(1).body().contains("\"after\":\"sku-9\""), requests.get(1).body());
    }

    @Test
    void productProjectionAlwaysIncludesTheSortField() {
        replies.add(new Reply(200, "[]"));

        client.searchProducts(new ProductSearchRequest(null, null, List.of("price"))).toList();

        assertTrue(requests.getFirst().body().contains("\"fields\":[\"price\",\"externalId\"]"),
                requests.getFirst().body());
    }

    @Test
    void rejectsProductSortsThatCanSkipTies() {
        var bySku = new ProductSearchRequest(new Pagination("sku", null, SortOrder.ASC, null), null, null);

        assertThrows(IllegalArgumentException.class, () -> client.searchProducts(bySku));
        assertTrue(requests.isEmpty());
    }

    @Test
    void timesOutWhenErliStopsResponding() {
        server.createContext("/slow", ex -> {
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ex.close();
        });
        var slowClient = new ErliClientBuilder()
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/slow")
                .token("test-key")
                .readTimeout(Duration.ofMillis(200))
                .build();

        assertThrows(ResourceAccessException.class, slowClient::getShopInfo);
    }

    @Test
    void inboxFetchAndMarkRead() {
        replies.add(new Reply(200, """
                [{"id":"5f9e1b3b0f0b9b0001c3e0a0","shopId":4,"created":"2026-03-26T12:50:11.664+01:00",
                  "read":false,"type":"productsNeedSync","payload":{"externalProductIds":["1"],"id":"h"}}]"""));
        replies.add(new Reply(200, "1"));

        List<InboxMessage> messages = client.fetchMessages();
        int marked = client.markMessagesRead(messages.getLast().id());

        assertEquals("productsNeedSync", messages.getFirst().type());
        assertNotNull(messages.getFirst().created());
        assertEquals(1, marked);
        assertEquals("{\"lastMessageId\":\"5f9e1b3b0f0b9b0001c3e0a0\"}", requests.get(1).body());
    }

    @Test
    void retriesOn429ThenSucceeds() {
        replies.add(new Reply(429, "{}"));
        replies.add(new Reply(200, product("a")));

        assertEquals("a", client.getProduct("a").externalId());
        assertEquals(2, requests.size());
    }

    @Test
    void notFoundRaisesApiException() {
        replies.add(new Reply(404, "{\"message\":\"not found\"}"));

        var ex = assertThrows(ErliApiException.class, () -> client.getProduct("missing"));
        assertEquals(404, ex.statusCode());
    }

    @Test
    void streamSurfacesApiErrors() {
        replies.add(new Reply(400, "{\"message\":\"bad filter\"}"));

        assertThrows(ErliApiException.class,
                () -> client.searchProducts(new ProductSearchRequest(null, null, null)).toList());
    }

    @Test
    void streamCategoriesPagesByIdAtMaxLimit() {
        var firstPage = new StringBuilder("[");
        for (int id = 1; id <= 200; id++) {
            if (id > 1) firstPage.append(',');
            firstPage.append(category(id));
        }
        replies.add(new Reply(200, firstPage.append(']').toString()));
        replies.add(new Reply(200, "[" + category(201) + "]"));
        replies.add(new Reply(200, "[]"));

        List<Category> categories = client.streamCategories().toList();

        assertEquals(201, categories.size());
        assertEquals("Meble", categories.getFirst().breadcrumb().getLast().name());
        assertTrue(categories.getFirst().leaf());
        assertEquals("/svc/shop-api/dictionaries/category/_search", requests.get(0).path());
        assertEquals("{\"limit\":200}", requests.get(0).body());
        assertEquals("{\"limit\":200,\"after\":200}", requests.get(1).body());
    }

    @Test
    void estimateCommissionSendsCategoryAndPrice() {
        replies.add(new Reply(200, "{\"commission\":850}"));

        assertEquals(850, client.estimateCommission(123, 10000, 2));

        Recorded r = requests.getFirst();
        assertEquals("POST", r.method());
        assertEquals("/svc/shop-api/commissions/_estimate", r.path());
        assertEquals("{\"categoryId\":123,\"quantity\":2,\"unitPrice\":10000}", r.body());
    }

    private static String category(int id) {
        return """
                {"id":%d,"name":"Kategoria %d","leaf":true,
                 "breadcrumb":[{"id":1,"name":"Dom i Ogród"},{"id":%d,"name":"Meble"}]}""".formatted(id, id, id);
    }

    private static String product(String id) {
        return """
                {"externalId":"%s","marketplaceId":1,"slug":"s","name":"Kurtka zimowa","ean":"5901234123457",
                 "externalAttributes":[],"externalCategories":[],"images":[{"url":"u","internalUrl":"i"}],"files":[],
                 "price":10000,"stock":3,"status":"active","dispatchTime":{"period":1},"attributes":[],
                 "categories":[[{"id":1,"name":"Dom"}]],"buyableProblems":[],"frozen":{"price":true},
                 "producerIds":[1],"translations":{"pl":{"name":"x","attributes":[]}},
                 "created":"2026-03-26T12:50:11.664+01:00"}""".formatted(id);
    }

    private static String order(String id, String cursor) {
        return """
                {"id":"%s","status":"purchased","items":[{"id":1,"externalId":"a","quantity":1,"unitPrice":100,
                 "name":"n","slug":"s"}],"delivery":{"name":"Paczkomat","typeId":"paczkomat","price":999,"cod":false},
                 "totalPrice":1099,"sellerStatus":"created","created":"2026-03-26T12:50:11.664+01:00",
                 "updated":"2026-03-26T12:50:11.664+01:00","cursor":"%s"}""".formatted(id, cursor);
    }
}
