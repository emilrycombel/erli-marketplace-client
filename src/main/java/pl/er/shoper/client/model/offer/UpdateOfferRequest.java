package pl.er.shoper.client.model.offer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateOfferRequest(
        @JsonProperty("name") String name,
        @JsonProperty("ean") String ean,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("stock") Integer stock,
        @JsonProperty("active") Integer active
) {
}
