package pl.er.shoper.client.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AuthServiceTest {

    @Test
    void authenticateReturnsTokenAndStoresIt() {
        var restClientBuilder = RestClient.builder().baseUrl("https://shop.example.com");
        var server = MockRestServiceServer.bindTo(restClientBuilder).build();
        var restClient = restClientBuilder.build();
        var tokenStore = new InMemoryTokenStore();

        server.expect(requestTo("https://shop.example.com/webapi/rest/auth"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Basic dXNlcjpwYXNz"))
                .andRespond(withSuccess("""
                        {"access_token": "new-token-abc", "expires_in": 3600, "token_type": "bearer"}
                        """, MediaType.APPLICATION_JSON));

        var authService = new AuthService(restClient, "user", "pass", tokenStore);
        var token = authService.authenticate();

        assertEquals("new-token-abc", token);
        assertTrue(tokenStore.load().isPresent());
        assertEquals("new-token-abc", tokenStore.load().get().accessToken());
        server.verify();
    }

    @Test
    void getValidTokenReturnsCachedTokenWhenValid() {
        var restClient = RestClient.builder().baseUrl("https://shop.example.com").build();
        var tokenStore = new InMemoryTokenStore();
        tokenStore.store(new StoredToken("cached-token", Instant.now().plusSeconds(3600)));

        var authService = new AuthService(restClient, "user", "pass", tokenStore);
        var token = authService.getValidToken();

        assertEquals("cached-token", token);
    }

    @Test
    void getValidTokenRefreshesExpiredToken() {
        var restClientBuilder = RestClient.builder().baseUrl("https://shop.example.com");
        var server = MockRestServiceServer.bindTo(restClientBuilder).build();
        var restClient = restClientBuilder.build();
        var tokenStore = new InMemoryTokenStore();
        tokenStore.store(new StoredToken("expired-token", Instant.now().minusSeconds(100)));

        server.expect(requestTo("https://shop.example.com/webapi/rest/auth"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"access_token": "refreshed-token", "expires_in": 3600, "token_type": "bearer"}
                        """, MediaType.APPLICATION_JSON));

        var authService = new AuthService(restClient, "user", "pass", tokenStore);
        var token = authService.getValidToken();

        assertEquals("refreshed-token", token);
        server.verify();
    }
}
