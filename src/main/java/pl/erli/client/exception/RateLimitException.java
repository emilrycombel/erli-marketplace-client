package pl.erli.client.exception;

import java.net.URI;
import java.time.Duration;

public class RateLimitException extends ErliApiException {

    private final Duration retryAfter;

    public RateLimitException(String responseBody, URI requestUri, Duration retryAfter) {
        super(429, responseBody, requestUri);
        this.retryAfter = retryAfter;
    }

    public Duration retryAfter() {
        return retryAfter;
    }
}
