package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Order(
        String id,
        String externalOrderId,
        String status,
        OrderUser user,
        List<OrderItem> items,
        Map<String, Object> rebate,
        OrderDelivery delivery,
        String comment,
        Integer totalPrice,
        DeliveryTracking deliveryTracking,
        Map<String, Object> payment,
        List<Map<String, Object>> returns,
        Integer calculatedParcelsCount,
        String sellerStatus,
        OffsetDateTime created,
        OffsetDateTime updated,
        OffsetDateTime purchasedAt,
        String cursor
) {}
