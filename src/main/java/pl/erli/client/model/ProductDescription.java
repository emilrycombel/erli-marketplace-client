package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductDescription(List<Section> sections) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Section(List<Item> items) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(String type, String content, String url) {

        public static Item text(String htmlContent) {
            return new Item("TEXT", htmlContent, null);
        }

        public static Item image(String imageUrl) {
            return new Item("IMAGE", null, imageUrl);
        }
    }

    public static ProductDescription of(Section... sections) {
        return new ProductDescription(List.of(sections));
    }

    public static Section section(Item... items) {
        return new Section(List.of(items));
    }
}
