package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductBatchUpdate(
        String externalId,
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
) {}
