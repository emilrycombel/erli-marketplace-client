package pl.erli.client.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PersistedTokenProvider implements TokenProvider {

    private static final Logger log = LogManager.getLogger(PersistedTokenProvider.class);

    private final TokenStore tokenStore;
    private final Object lock = new Object();
    private volatile String cachedToken;

    public PersistedTokenProvider(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public String getToken() {
        String token = cachedToken;
        if (token != null) {
            return token;
        }
        synchronized (lock) {
            if (cachedToken == null) {
                cachedToken = tokenStore.load().orElse(null);
                if (cachedToken != null) {
                    log.debug("Token loaded from store");
                }
            }
            token = cachedToken;
        }
        if (token == null) {
            throw new IllegalStateException("No token available in store");
        }
        return token;
    }

    public void saveToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }
        synchronized (lock) {
            tokenStore.save(token);
            cachedToken = token;
        }
    }

    public void invalidate() {
        synchronized (lock) {
            cachedToken = null;
        }
    }
}
