package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InboxMessage(
        String id,
        Integer shopId,
        OffsetDateTime created,
        Boolean read,
        String type,
        Map<String, Object> payload
) {}
