package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductUpdate(
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
        Map<String, Boolean> frozen,
        String newExternalId,
        Object overrideFrozen
) {
    // index: 0    1    2    3    4    5    6    7    8    9    10
    //        11   12   13   14   15   16   17   18   19   20   21
    //        22   23   24   25   26   27   28   29   30   31   32
    //        33   34   35   36   37   38   39   40   41
    public static ProductUpdate withStock(int stock) {
        return new ProductUpdate(
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, stock, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    public static ProductUpdate withPrice(int priceInGrosze) {
        return new ProductUpdate(
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, priceInGrosze, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    public static ProductUpdate withStatus(String status) {
        return new ProductUpdate(
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, status, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }
}
