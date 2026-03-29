package pl.er.shoper.client.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Base64;

public class AuthService {

    private static final Logger log = LogManager.getLogger(AuthService.class);

    private final RestClient restClient;
    private final String username;
    private final String password;
    private final TokenStore tokenStore;

    public AuthService(RestClient restClient, String username, String password, TokenStore tokenStore) {
        this.restClient = restClient;
        this.username = username;
        this.password = password;
        this.tokenStore = tokenStore;
    }

    public String getValidToken() {
        var stored = tokenStore.load();
        if (stored.isPresent() && stored.get().isValid()) {
            log.debug("Using cached token, expires at {}", stored.get().expiresAt());
            return stored.get().accessToken();
        }
        return authenticate();
    }

    public String authenticate() {
        log.info("Authenticating with Shoper API");
        var credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        var response = restClient.post()
                .uri("/webapi/rest/auth")
                .header("Authorization", "Basic " + credentials)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null) {
            throw new ShoperAuthenticationException("Authentication failed: empty response");
        }

        var storedToken = new StoredToken(response.accessToken(), Instant.now().plusSeconds(response.expiresIn()));
        tokenStore.store(storedToken);
        log.info("Successfully authenticated, token expires in {} seconds", response.expiresIn());
        return response.accessToken();
    }
}
