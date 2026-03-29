package pl.erli.client.auth;

public record StaticTokenProvider(String token) implements TokenProvider {

    public StaticTokenProvider {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }
    }

    @Override
    public String getToken() {
        return token;
    }
}
