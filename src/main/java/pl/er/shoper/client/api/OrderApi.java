package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.model.order.Order;

public class OrderApi extends BaseApi<Order> {

    private static final Logger log = LogManager.getLogger(OrderApi.class);
    private static final String RESOURCE_PATH = "/webapi/rest/orders";

    public OrderApi(RestClient restClient, ObjectMapper objectMapper) {
        super(restClient, objectMapper, RESOURCE_PATH, Order.class);
    }

    public Order findById(long id) {
        log.debug("Fetching order {}", id);
        return restClient.get()
                .uri(RESOURCE_PATH + "/{id}", id)
                .retrieve()
                .body(Order.class);
    }
}
