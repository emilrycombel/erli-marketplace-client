package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.model.message.Message;
import pl.er.shoper.client.model.message.SendMessageRequest;

public class MessageApi extends BaseApi<Message> {

    private static final Logger log = LogManager.getLogger(MessageApi.class);
    private static final String RESOURCE_PATH = "/webapi/rest/messages";

    public MessageApi(RestClient restClient, ObjectMapper objectMapper) {
        super(restClient, objectMapper, RESOURCE_PATH, Message.class);
    }

    public Message findById(long id) {
        log.debug("Fetching message {}", id);
        return restClient.get()
                .uri(RESOURCE_PATH + "/{id}", id)
                .retrieve()
                .body(Message.class);
    }

    public Message send(SendMessageRequest request) {
        log.debug("Sending message");
        return restClient.post()
                .uri(RESOURCE_PATH)
                .body(request)
                .retrieve()
                .body(Message.class);
    }
}
