package pl.er.shoper.client.model.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateProductRequest(
        @JsonProperty("code") String code,
        @JsonProperty("ean") String ean,
        @JsonProperty("producer_id") Long producerId,
        @JsonProperty("category_id") Long categoryId,
        @JsonProperty("unit_id") Long unitId,
        @JsonProperty("tax_id") Long taxId,
        @JsonProperty("stock") StockData stock,
        @JsonProperty("translations") Map<String, TranslationData> translations
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StockData(
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("stock") BigDecimal stockLevel,
            @JsonProperty("weight") BigDecimal weight
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TranslationData(
            @JsonProperty("name") String name,
            @JsonProperty("short_description") String shortDescription,
            @JsonProperty("description") String description,
            @JsonProperty("active") Integer active
    ) {
    }
}
