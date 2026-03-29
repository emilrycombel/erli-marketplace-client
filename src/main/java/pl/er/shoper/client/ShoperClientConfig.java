package pl.er.shoper.client;

import pl.er.shoper.client.auth.InMemoryTokenStore;
import pl.er.shoper.client.auth.TokenStore;

public record ShoperClientConfig(
        String baseUrl,
        String username,
        String password,
        TokenStore tokenStore
) {

    public ShoperClientConfig(String baseUrl, String username, String password) {
        this(baseUrl, username, password, new InMemoryTokenStore());
    }
}
