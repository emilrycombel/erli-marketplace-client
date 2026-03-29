package pl.er.shoper.client.model.offer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OfferStockUpdate(
        @JsonProperty("stock") int stock
) {
}
