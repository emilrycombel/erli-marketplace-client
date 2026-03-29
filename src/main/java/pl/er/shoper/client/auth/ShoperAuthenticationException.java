package pl.er.shoper.client.auth;

public class ShoperAuthenticationException extends RuntimeException {

    public ShoperAuthenticationException(String message) {
        super(message);
    }

    public ShoperAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
