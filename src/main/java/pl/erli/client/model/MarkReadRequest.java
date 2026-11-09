package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MarkReadRequest(
        String lastMessageId,
        List<String> ids
) {
    public static MarkReadRequest byLastId(String lastMessageId) {
        return new MarkReadRequest(lastMessageId, null);
    }

    public static MarkReadRequest byIds(List<String> ids) {
        return new MarkReadRequest(null, ids);
    }
}
