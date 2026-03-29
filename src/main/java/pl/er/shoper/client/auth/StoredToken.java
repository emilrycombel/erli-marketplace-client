package pl.er.shoper.client.auth;

import java.time.Instant;

public record StoredToken(
        String accessToken,
        Instant expiresAt
) {

    public boolean isValid() {
        return accessToken != null && Instant.now().plusSeconds(60).isBefore(expiresAt);
    }
}
