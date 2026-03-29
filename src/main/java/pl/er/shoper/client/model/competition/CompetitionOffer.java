package pl.er.shoper.client.model.competition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompetitionOffer(
        @JsonProperty("offer_id") Long offerId,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("seller") String seller,
        @JsonProperty("name") String name,
        @JsonProperty("ean") String ean,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("stock") Integer stock
) {
}
