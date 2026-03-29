package pl.er.shoper.client.model.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Product(
        @JsonProperty("product_id") Long productId,
        @JsonProperty("code") String code,
        @JsonProperty("ean") String ean,
        @JsonProperty("producer_id") Long producerId,
        @JsonProperty("category_id") Long categoryId,
        @JsonProperty("unit_id") Long unitId,
        @JsonProperty("tax_id") Long taxId,
        @JsonProperty("stock") ProductStock stock,
        @JsonProperty("translations") java.util.Map<String, ProductTranslation> translations
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductStock(
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("stock") BigDecimal stockLevel,
            @JsonProperty("weight") BigDecimal weight
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductTranslation(
            @JsonProperty("name") String name,
            @JsonProperty("short_description") String shortDescription,
            @JsonProperty("description") String description,
            @JsonProperty("active") Integer active
    ) {
    }
}
