package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductCreate(
        String name,
        Object description,
        String ean,
        String sku,
        List<Map<String, Object>> externalReferences,
        Integer sourceFulfillmentProductId,
        List<String> importantFeatures,
        List<Map<String, Object>> externalAttributes,
        List<Map<String, Object>> externalCategories,
        Map<String, Object> externalVariantGroup,
        List<Map<String, Object>> externalResponsibleProducer,
        List<Map<String, Object>> externalResponsiblePerson,
        List<ProductImage> images,
        List<Map<String, Object>> files,
        Integer price,
        Integer mobilePrice,
        Integer cataloguePrice,
        String referencePriceType,
        Integer stock,
        String status,
        Boolean archived,
        DispatchTime dispatchTime,
        String deliveryPriceList,
        Double weight,
        String obligatoryIdentifier,
        String voluntaryIdentifier,
        String returnIdentifier,
        String invoiceType,
        String taxRate,
        Integer basketLimit,
        String energyLabel,
        String instructionWithSafetyInformation,
        String informationCard,
        Integer producerId,
        Integer responsiblePersonId,
        String externalMetaProductId,
        List<Map<String, Object>> productAttachments,
        Integer automaticDiscountRuleId,
        Map<String, Object> packaging,
        Map<String, Boolean> frozen
) {
    public ProductCreate {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(images, "images is required");
        Objects.requireNonNull(price, "price is required");
        Objects.requireNonNull(stock, "stock is required");
        Objects.requireNonNull(dispatchTime, "dispatchTime is required");
    }

    public static Builder builder(String name, List<ProductImage> images,
                                   int price, int stock, DispatchTime dispatchTime) {
        return new Builder(name, images, price, stock, dispatchTime);
    }

    public static final class Builder {
        private final String name;
        private final List<ProductImage> images;
        private final Integer price;
        private final Integer stock;
        private final DispatchTime dispatchTime;
        private Object description;
        private String ean;
        private String sku;
        private List<Map<String, Object>> externalReferences;
        private Integer sourceFulfillmentProductId;
        private List<String> importantFeatures;
        private List<Map<String, Object>> externalAttributes;
        private List<Map<String, Object>> externalCategories;
        private Map<String, Object> externalVariantGroup;
        private List<Map<String, Object>> externalResponsibleProducer;
        private List<Map<String, Object>> externalResponsiblePerson;
        private List<Map<String, Object>> files;
        private Integer mobilePrice;
        private Integer cataloguePrice;
        private String referencePriceType;
        private String status;
        private Boolean archived;
        private String deliveryPriceList;
        private Double weight;
        private String obligatoryIdentifier;
        private String voluntaryIdentifier;
        private String returnIdentifier;
        private String invoiceType;
        private String taxRate;
        private Integer basketLimit;
        private String energyLabel;
        private String instructionWithSafetyInformation;
        private String informationCard;
        private Integer producerId;
        private Integer responsiblePersonId;
        private String externalMetaProductId;
        private List<Map<String, Object>> productAttachments;
        private Integer automaticDiscountRuleId;
        private Map<String, Object> packaging;
        private Map<String, Boolean> frozen;

        private Builder(String name, List<ProductImage> images,
                        int price, int stock, DispatchTime dispatchTime) {
            this.name = name;
            this.images = images;
            this.price = price;
            this.stock = stock;
            this.dispatchTime = dispatchTime;
        }

        public Builder description(Object v) { this.description = v; return this; }
        public Builder ean(String v) { this.ean = v; return this; }
        public Builder sku(String v) { this.sku = v; return this; }
        public Builder externalReferences(List<Map<String, Object>> v) { this.externalReferences = v; return this; }
        public Builder sourceFulfillmentProductId(Integer v) { this.sourceFulfillmentProductId = v; return this; }
        public Builder importantFeatures(List<String> v) { this.importantFeatures = v; return this; }
        public Builder externalAttributes(List<Map<String, Object>> v) { this.externalAttributes = v; return this; }
        public Builder externalCategories(List<Map<String, Object>> v) { this.externalCategories = v; return this; }
        public Builder externalVariantGroup(Map<String, Object> v) { this.externalVariantGroup = v; return this; }
        public Builder externalResponsibleProducer(List<Map<String, Object>> v) { this.externalResponsibleProducer = v; return this; }
        public Builder externalResponsiblePerson(List<Map<String, Object>> v) { this.externalResponsiblePerson = v; return this; }
        public Builder files(List<Map<String, Object>> v) { this.files = v; return this; }
        public Builder mobilePrice(Integer v) { this.mobilePrice = v; return this; }
        public Builder cataloguePrice(Integer v) { this.cataloguePrice = v; return this; }
        public Builder referencePriceType(String v) { this.referencePriceType = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Builder archived(Boolean v) { this.archived = v; return this; }
        public Builder deliveryPriceList(String v) { this.deliveryPriceList = v; return this; }
        public Builder weight(Double v) { this.weight = v; return this; }
        public Builder obligatoryIdentifier(String v) { this.obligatoryIdentifier = v; return this; }
        public Builder voluntaryIdentifier(String v) { this.voluntaryIdentifier = v; return this; }
        public Builder returnIdentifier(String v) { this.returnIdentifier = v; return this; }
        public Builder invoiceType(String v) { this.invoiceType = v; return this; }
        public Builder taxRate(String v) { this.taxRate = v; return this; }
        public Builder basketLimit(Integer v) { this.basketLimit = v; return this; }
        public Builder energyLabel(String v) { this.energyLabel = v; return this; }
        public Builder instructionWithSafetyInformation(String v) { this.instructionWithSafetyInformation = v; return this; }
        public Builder informationCard(String v) { this.informationCard = v; return this; }
        public Builder producerId(Integer v) { this.producerId = v; return this; }
        public Builder responsiblePersonId(Integer v) { this.responsiblePersonId = v; return this; }
        public Builder externalMetaProductId(String v) { this.externalMetaProductId = v; return this; }
        public Builder productAttachments(List<Map<String, Object>> v) { this.productAttachments = v; return this; }
        public Builder automaticDiscountRuleId(Integer v) { this.automaticDiscountRuleId = v; return this; }
        public Builder packaging(Map<String, Object> v) { this.packaging = v; return this; }
        public Builder frozen(Map<String, Boolean> v) { this.frozen = v; return this; }

        public ProductCreate build() {
            return new ProductCreate(name, description, ean, sku, externalReferences,
                    sourceFulfillmentProductId, importantFeatures,
                    externalAttributes, externalCategories, externalVariantGroup,
                    externalResponsibleProducer, externalResponsiblePerson,
                    images, files, price, mobilePrice, cataloguePrice, referencePriceType,
                    stock, status, archived, dispatchTime, deliveryPriceList, weight,
                    obligatoryIdentifier, voluntaryIdentifier, returnIdentifier,
                    invoiceType, taxRate, basketLimit, energyLabel,
                    instructionWithSafetyInformation, informationCard,
                    producerId, responsiblePersonId, externalMetaProductId,
                    productAttachments, automaticDiscountRuleId, packaging, frozen);
        }
    }
}
