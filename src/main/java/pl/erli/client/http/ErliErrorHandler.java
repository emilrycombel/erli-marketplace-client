package pl.erli.client.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.erli.client.exception.ErliApiException;
import pl.erli.client.exception.RateLimitException;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class ErliErrorHandler implements ResponseErrorHandler {

    private static final Logger log = LogManager.getLogger(ErliErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(URI url, org.springframework.http.HttpMethod method,
                            ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        String body = new String(response.getBody().readAllBytes());

        if (status == 429) {
            Duration retryAfter = parseRetryAfter(response);
            log.warn("Rate limited (429) for {} {}, retry after {}", method, url, retryAfter);
            throw new RateLimitException(body, url, retryAfter);
        }

        log.error("Erli API error {} for {} {}: {}", status, method, url, body);
        throw new ErliApiException(status, body, url);
    }

    private Duration parseRetryAfter(ClientHttpResponse response) {
        String header = response.getHeaders().getFirst("Retry-After");
        if (header != null) {
            try {
                return Duration.ofSeconds(Long.parseLong(header));
            } catch (NumberFormatException ignored) {
            }
        }
        return Duration.ofSeconds(1);
    }
}
