package pl.er.shoper.client.model.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderProduct(
        @JsonProperty("product_id") Long productId,
        @JsonProperty("name") String name,
        @JsonProperty("code") String code,
        @JsonProperty("quantity") Integer quantity,
        @JsonProperty("price") BigDecimal price
) {
}
