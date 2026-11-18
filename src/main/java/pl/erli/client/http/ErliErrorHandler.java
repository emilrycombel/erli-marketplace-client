package pl.erli.client.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.erli.client.exception.ErliApiException;
import pl.erli.client.exception.RateLimitException;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class ErliErrorHandler implements ResponseErrorHandler {

    private static final Logger log = LogManager.getLogger(ErliErrorHandler.class);
    private static final Duration DEFAULT_RETRY_AFTER = Duration.ofSeconds(1);

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

    static Duration parseRetryAfter(ClientHttpResponse response) {
        String header = response.getHeaders().getFirst("Retry-After");
        if (header == null) {
            return DEFAULT_RETRY_AFTER;
        }
        String value = header.trim();
        try {
            return Duration.ofSeconds(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
        }
        try {
            Instant at = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
            Duration wait = Duration.between(Instant.now(), at);
            return wait.isNegative() ? Duration.ZERO : wait;
        } catch (DateTimeParseException ignored) {
        }
        return DEFAULT_RETRY_AFTER;
    }
}
