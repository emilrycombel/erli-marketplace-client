package pl.erli.client;

import pl.erli.client.auth.*;
import pl.erli.client.http.ErliRestClientFactory;

import java.nio.file.Path;

public class ErliClientBuilder {

    private String baseUrl = "https://erli.pl/svc/shop-api";
    private TokenProvider tokenProvider;
    private TokenStore tokenStore;
    private String token;
    private Path tokenFile;
    private int maxRetries = 3;
    private long initialDelayMs = 1000;
    private int pageSize = 50;
    private int prefetchPages = 2;

    public ErliClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ErliClientBuilder tokenProvider(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        return this;
    }

    public ErliClientBuilder tokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
        return this;
    }

    public ErliClientBuilder token(String token) {
        this.token = token;
        return this;
    }

    public ErliClientBuilder tokenFile(Path tokenFile) {
        this.tokenFile = tokenFile;
        return this;
    }

    public ErliClientBuilder maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public ErliClientBuilder initialDelayMs(long initialDelayMs) {
        this.initialDelayMs = initialDelayMs;
        return this;
    }

    public ErliClientBuilder pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ErliClientBuilder prefetchPages(int prefetchPages) {
        this.prefetchPages = prefetchPages;
        return this;
    }

    public ErliClient build() {
        TokenProvider resolvedProvider = resolveTokenProvider();
        var restClient = ErliRestClientFactory.create(baseUrl, resolvedProvider, maxRetries, initialDelayMs);
        return new ErliClient(restClient, pageSize, prefetchPages);
    }

    private TokenProvider resolveTokenProvider() {
        if (tokenProvider != null) {
            return tokenProvider;
        }
        if (tokenStore != null) {
            return new PersistedTokenProvider(tokenStore);
        }
        if (tokenFile != null) {
            return new PersistedTokenProvider(new FileTokenStore(tokenFile));
        }
        if (token != null) {
            return new StaticTokenProvider(token);
        }
        throw new IllegalStateException(
                "No token configuration provided. Set token, tokenFile, tokenStore, or tokenProvider.");
    }
}
