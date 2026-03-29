package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.model.product.CreateProductRequest;
import pl.er.shoper.client.model.product.Product;

public class ProductApi extends BaseApi<Product> {

    private static final Logger log = LogManager.getLogger(ProductApi.class);
    private static final String RESOURCE_PATH = "/webapi/rest/products";

    public ProductApi(RestClient restClient, ObjectMapper objectMapper) {
        super(restClient, objectMapper, RESOURCE_PATH, Product.class);
    }

    public Product findById(long id) {
        log.debug("Fetching product {}", id);
        return restClient.get()
                .uri(RESOURCE_PATH + "/{id}", id)
                .retrieve()
                .body(Product.class);
    }

    public Product create(CreateProductRequest request) {
        log.debug("Creating product");
        return restClient.post()
                .uri(RESOURCE_PATH)
                .body(request)
                .retrieve()
                .body(Product.class);
    }
}
