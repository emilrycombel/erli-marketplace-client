package pl.er.shoper.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pl.er.shoper.client.model.message.SendMessageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MessageApiTest {

    private MockRestServiceServer server;
    private MessageApi messageApi;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("https://shop.example.com");
        server = MockRestServiceServer.bindTo(builder).build();
        messageApi = new MessageApi(builder.build(), new ObjectMapper());
    }

    @Test
    void findByIdReturnsMessage() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/messages/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"message_id": 5, "subject": "Hello", "body": "World", "sender": "admin", "read": 0}
                        """, MediaType.APPLICATION_JSON));

        var message = messageApi.findById(5);

        assertEquals(5L, message.messageId());
        assertEquals("Hello", message.subject());
        assertEquals("World", message.body());
        server.verify();
    }

    @Test
    void sendCreatesMessage() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/messages"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"message_id": 10, "subject": "Test", "body": "Content", "recipient": "user@example.com"}
                        """, MediaType.APPLICATION_JSON));

        var request = new SendMessageRequest("Test", "Content", "user@example.com");
        var message = messageApi.send(request);

        assertEquals(10L, message.messageId());
        assertEquals("Test", message.subject());
        server.verify();
    }

    @Test
    void findAllReturnsPaginatedMessages() {
        server.expect(requestTo("https://shop.example.com/webapi/rest/messages?page=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"count": 1, "pages": 1, "page": 1, "list": [
                            {"message_id": 1, "subject": "Msg 1", "body": "Body 1"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        var messages = messageApi.findAll().toList();

        assertEquals(1, messages.size());
        assertEquals("Msg 1", messages.getFirst().subject());
        server.verify();
    }
}
