package pl.erli.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.erli.client.model.*;
import pl.erli.client.pagination.PaginatedStream;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Stream;

public class ErliClient {

    private static final Logger log = LogManager.getLogger(ErliClient.class);

    private final RestClient restClient;
    private final int defaultPageSize;
    private final int prefetchPages;

    public ErliClient(RestClient restClient, int defaultPageSize, int prefetchPages) {
        this.restClient = restClient;
        this.defaultPageSize = defaultPageSize;
        this.prefetchPages = prefetchPages;
    }

    // --- Shop ---

    public ShopResponse getShopInfo() {
        log.debug("GET /me");
        return restClient.get()
                .uri("/me")
                .retrieve()
                .body(ShopResponse.class);
    }

    // --- Products ---

    public Product getProduct(String externalId) {
        log.debug("GET /products/{}", externalId);
        return restClient.get()
                .uri("/products/{externalId}", externalId)
                .retrieve()
                .body(Product.class);
    }

    public Product getProduct(String externalId, List<String> fields) {
        log.debug("GET /products/{} fields={}", externalId, fields);
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/products/{externalId}")
                        .queryParam("fields", fields)
                        .build(externalId))
                .retrieve()
                .body(Product.class);
    }

    public Stream<Product> searchProducts(ProductSearchRequest request) {
        log.debug("Streaming products search");
        int pageSize = request.pagination() != null && request.pagination().limit() != null
                ? request.pagination().limit() : defaultPageSize;

        return PaginatedStream.create(cursor -> {
            Pagination pagination = request.pagination() != null
                    ? request.pagination().withAfter(cursor)
                    : new Pagination(null, cursor, null, pageSize);
            if (pagination.limit() == null) {
                pagination = new Pagination(pagination.sortField(), pagination.after(),
                        pagination.order(), pageSize);
            }
            var searchReq = new ProductSearchRequest(pagination, request.filter(), request.fields());
            List<Product> products = restClient.post()
                    .uri("/products/_search")
                    .body(searchReq)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Product>>() {});
            if (products == null || products.isEmpty()) {
                return new PaginatedStream.Page<>(List.of(), null);
            }
            Product last = products.getLast();
            Object nextCursor = resolveProductCursor(last, pagination.sortField());
            return new PaginatedStream.Page<>(products, nextCursor);
        }, pageSize, prefetchPages);
    }

    public void createProduct(String externalId, ProductCreate product) {
        log.debug("POST /products/{}", externalId);
        restClient.post()
                .uri("/products/{externalId}", externalId)
                .body(product)
                .retrieve()
                .toBodilessEntity();
    }

    public ProductUpdateResponse updateProduct(String externalId, ProductUpdate update) {
        log.debug("PATCH /products/{}", externalId);
        return restClient.patch()
                .uri("/products/{externalId}", externalId)
                .body(update)
                .retrieve()
                .body(ProductUpdateResponse.class);
    }

    public List<ProductBatchResponse> batchUpdateProducts(List<ProductBatchUpdate> updates) {
        log.debug("PATCH /products/batch-update ({} products)", updates.size());
        return restClient.patch()
                .uri("/products/batch-update")
                .body(updates)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // --- Convenience offer methods ---

    public ProductUpdateResponse updateStock(String externalId, int stock) {
        return updateProduct(externalId, ProductUpdate.withStock(stock));
    }

    public ProductUpdateResponse updatePrice(String externalId, int priceInGrosze) {
        return updateProduct(externalId, ProductUpdate.withPrice(priceInGrosze));
    }

    public ProductUpdateResponse deactivateProduct(String externalId) {
        return updateProduct(externalId, ProductUpdate.withStatus("inactive"));
    }

    // --- Competition by EAN ---

    public Stream<Product> findByEan(String ean) {
        log.debug("Searching products by EAN: {}", ean);
        var request = new ProductSearchRequest(
                new Pagination("externalId", null, SortOrder.ASC, defaultPageSize),
                Filter.eq("ean", ean),
                null
        );
        return searchProducts(request);
    }

    // --- Orders ---

    public Order getOrder(String orderId) {
        log.debug("GET /orders/{}", orderId);
        return restClient.get()
                .uri("/orders/{id}", orderId)
                .retrieve()
                .body(Order.class);
    }

    public Stream<Order> searchOrders(OrderSearchRequest request) {
        log.debug("Streaming orders search");
        int pageSize = request.pagination() != null && request.pagination().limit() != null
                ? request.pagination().limit() : defaultPageSize;

        return PaginatedStream.create(cursor -> {
            Pagination pagination = request.pagination() != null
                    ? request.pagination().withAfter(cursor)
                    : new Pagination("updated", cursor, SortOrder.ASC, pageSize);
            if (pagination.limit() == null) {
                pagination = new Pagination(pagination.sortField(), pagination.after(),
                        pagination.order(), pageSize);
            }
            var searchReq = new OrderSearchRequest(pagination, request.filter());
            List<Order> orders = restClient.post()
                    .uri("/orders/_search")
                    .body(searchReq)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Order>>() {});
            if (orders == null || orders.isEmpty()) {
                return new PaginatedStream.Page<>(List.of(), null);
            }
            String nextCursor = orders.getLast().cursor();
            return new PaginatedStream.Page<>(orders, nextCursor);
        }, pageSize, prefetchPages);
    }

    public void updateOrderStatus(String orderId, String status) {
        log.debug("PATCH /orders/{}/status -> {}", orderId, status);
        restClient.patch()
                .uri("/orders/{id}/status", orderId)
                .body(new OrderUpdateStatus(status))
                .retrieve()
                .toBodilessEntity();
    }

    // --- Inbox (messages) ---

    public List<InboxMessage> fetchMessages() {
        log.debug("GET /inbox");
        return restClient.get()
                .uri("/inbox")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<InboxMessage> searchMessages(InboxSearchRequest request) {
        log.debug("POST /inbox/_search");
        return restClient.post()
                .uri("/inbox/_search")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public int markMessagesRead(String lastMessageId) {
        log.debug("POST /inbox/mark-read lastMessageId={}", lastMessageId);
        Integer count = restClient.post()
                .uri("/inbox/mark-read")
                .body(MarkReadRequest.byLastId(lastMessageId))
                .retrieve()
                .body(Integer.class);
        return count != null ? count : 0;
    }

    public int markMessagesRead(List<String> ids) {
        log.debug("POST /inbox/mark-read ids={}", ids.size());
        Integer count = restClient.post()
                .uri("/inbox/mark-read")
                .body(MarkReadRequest.byIds(ids))
                .retrieve()
                .body(Integer.class);
        return count != null ? count : 0;
    }

    // --- Private helpers ---

    private Object resolveProductCursor(Product product, String sortField) {
        if (sortField == null) return product.externalId();
        return switch (sortField) {
            case "externalId" -> product.externalId();
            case "marketplaceId" -> product.marketplaceId();
            case "name" -> product.name();
            case "ean" -> product.ean();
            case "sku" -> product.sku();
            case "created" -> product.created();
            case "updated" -> product.updated();
            case "archivedAt" -> product.archivedAt();
            default -> product.externalId();
        };
    }
}
