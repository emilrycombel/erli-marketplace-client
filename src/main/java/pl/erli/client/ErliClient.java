package pl.erli.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.erli.client.model.*;
import pl.erli.client.pagination.PaginatedStream;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ErliClient {

    private static final Logger log = LogManager.getLogger(ErliClient.class);
    private static final int CATEGORY_PAGE_SIZE = 200;
    private static final Set<String> UNIQUE_PRODUCT_SORT_FIELDS = Set.of("externalId", "marketplaceId");

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
        Pagination requested = request.pagination() != null
                ? request.pagination() : new Pagination(null, null, null, null);
        String sortField = requested.sortField() != null ? requested.sortField() : "externalId";
        if (!UNIQUE_PRODUCT_SORT_FIELDS.contains(sortField)) {
            throw new IllegalArgumentException("Product streams must sort by externalId or marketplaceId. "
                    + "Sorting by " + sortField + " skips products that share a value across a page boundary; "
                    + "filter on " + sortField + " instead.");
        }
        Pagination first = withDefaultLimit(requested);
        List<String> fields = includeField(request.fields(), sortField);
        log.debug("Streaming products search sorted by {}", sortField);

        return PaginatedStream.create(cursor -> {
            var searchReq = new ProductSearchRequest(
                    cursor == null ? first : first.withAfter(cursor), request.filter(), fields);
            List<Product> products = restClient.post()
                    .uri("/products/_search")
                    .body(searchReq)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Product>>() {});
            if (products == null || products.isEmpty()) {
                return new PaginatedStream.Page<>(List.of(), null);
            }
            Product last = products.getLast();
            Object nextCursor = "marketplaceId".equals(sortField) ? last.marketplaceId() : last.externalId();
            return new PaginatedStream.Page<>(products, nextCursor);
        }, prefetchPages);
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
        Pagination first = withDefaultLimit(request.pagination() != null
                ? request.pagination() : new Pagination("updated", null, SortOrder.ASC, null));

        return PaginatedStream.create(cursor -> {
            var searchReq = new OrderSearchRequest(
                    cursor == null ? first : first.withAfter(cursor), request.filter());
            List<Order> orders = restClient.post()
                    .uri("/orders/_search")
                    .body(searchReq)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Order>>() {});
            if (orders == null || orders.isEmpty()) {
                return new PaginatedStream.Page<>(List.of(), null);
            }
            return new PaginatedStream.Page<>(orders, orders.getLast().cursor());
        }, prefetchPages);
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

    // --- Categories and commissions ---

    public Stream<Category> streamCategories() {
        log.debug("Streaming categories");
        return PaginatedStream.create(cursor -> {
            List<Category> categories = restClient.post()
                    .uri("/dictionaries/category/_search")
                    .body(new CategorySearchRequest(CATEGORY_PAGE_SIZE, (Integer) cursor))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Category>>() {});
            if (categories == null || categories.isEmpty()) {
                return new PaginatedStream.Page<>(List.of(), null);
            }
            return new PaginatedStream.Page<>(categories, categories.getLast().id());
        }, prefetchPages);
    }

    public int estimateCommission(int categoryId, int unitPrice, int quantity) {
        log.debug("POST /commissions/_estimate categoryId={} unitPrice={} quantity={}",
                categoryId, unitPrice, quantity);
        CommissionEstimate estimate = restClient.post()
                .uri("/commissions/_estimate")
                .body(new EstimateCommissionRequest(categoryId, quantity, unitPrice))
                .retrieve()
                .body(CommissionEstimate.class);
        return estimate.commission();
    }

    // --- Private helpers ---

    private Pagination withDefaultLimit(Pagination pagination) {
        return pagination.limit() != null ? pagination
                : new Pagination(pagination.sortField(), pagination.after(), pagination.order(), defaultPageSize);
    }

    private static List<String> includeField(List<String> fields, String field) {
        if (fields == null || fields.contains(field)) {
            return fields;
        }
        var withField = new ArrayList<>(fields);
        withField.add(field);
        return withField;
    }
}
