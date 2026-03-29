package pl.er.shoper.client.model.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendMessageRequest(
        @JsonProperty("subject") String subject,
        @JsonProperty("body") String body,
        @JsonProperty("recipient") String recipient
) {
}
