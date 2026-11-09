package pl.erli.client.exception;

import java.net.URI;

public class ErliApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;
    private final URI requestUri;

    public ErliApiException(int statusCode, String responseBody, URI requestUri) {
        super("Erli API error %d for %s: %s".formatted(statusCode, requestUri, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.requestUri = requestUri;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }

    public URI requestUri() {
        return requestUri;
    }
}
