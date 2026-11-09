package pl.erli.client.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class RateLimitInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LogManager.getLogger(RateLimitInterceptor.class);

    private final int maxRetries;
    private final long initialDelayMs;

    public RateLimitInterceptor(int maxRetries, long initialDelayMs) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                         ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        int attempt = 0;
        while (response.getStatusCode().value() == 429 && attempt < maxRetries) {
            attempt++;
            long delay = initialDelayMs * (1L << (attempt - 1));
            log.info("Rate limited (429), retry {}/{} after {}ms for {} {}",
                    attempt, maxRetries, delay, request.getMethod(), request.getURI());
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return response;
            }
            response = execution.execute(request, body);
        }

        return response;
    }
}
