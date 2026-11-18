package pl.erli.client.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class ErliErrorHandlerTest {

    @Test
    void parsesRetryAfterSeconds() {
        assertEquals(Duration.ofSeconds(30), ErliErrorHandler.parseRetryAfter(withRetryAfter("30")));
    }

    @Test
    void parsesRetryAfterHttpDate() {
        String inThirtySeconds = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(30)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);

        Duration wait = ErliErrorHandler.parseRetryAfter(withRetryAfter(inThirtySeconds));

        assertTrue(wait.compareTo(Duration.ofSeconds(25)) > 0 && wait.compareTo(Duration.ofSeconds(31)) < 0,
                "unexpected wait " + wait);
    }

    @Test
    void fallsBackToOneSecondForUnparsableValues() {
        assertEquals(Duration.ofSeconds(1), ErliErrorHandler.parseRetryAfter(withRetryAfter("soon")));
    }

    private static MockClientHttpResponse withRetryAfter(String value) {
        var response = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Retry-After", value);
        return response;
    }
}
