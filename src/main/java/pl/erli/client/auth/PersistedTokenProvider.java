package pl.erli.client.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PersistedTokenProvider implements TokenProvider {

    private static final Logger log = LogManager.getLogger(PersistedTokenProvider.class);

    private final TokenStore tokenStore;
    private volatile String cachedToken;

    public PersistedTokenProvider(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public String getToken() {
        String token = cachedToken;
        if (token == null) {
            token = tokenStore.load().orElse(null);
            if (token != null) {
                cachedToken = token;
                log.debug("Token loaded from store");
            }
        }
        if (token == null) {
            throw new IllegalStateException("No token available in store");
        }
        return token;
    }

    public void saveToken(String token) {
        this.cachedToken = token;
        tokenStore.save(token);
    }

    public void invalidate() {
        this.cachedToken = null;
    }
}
