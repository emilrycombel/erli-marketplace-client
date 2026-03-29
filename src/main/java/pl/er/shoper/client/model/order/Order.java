package pl.er.shoper.client.model.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Order(
        @JsonProperty("order_id") Long orderId,
        @JsonProperty("status_id") Integer statusId,
        @JsonProperty("sum") BigDecimal sum,
        @JsonProperty("payment_id") Long paymentId,
        @JsonProperty("shipping_id") Long shippingId,
        @JsonProperty("email") String email,
        @JsonProperty("date") String date,
        @JsonProperty("products") List<OrderProduct> products
) {
}
