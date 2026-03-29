package pl.er.shoper.client.model.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
        @JsonProperty("message_id") Long messageId,
        @JsonProperty("subject") String subject,
        @JsonProperty("body") String body,
        @JsonProperty("sender") String sender,
        @JsonProperty("recipient") String recipient,
        @JsonProperty("date") String date,
        @JsonProperty("read") Integer read
) {
}
