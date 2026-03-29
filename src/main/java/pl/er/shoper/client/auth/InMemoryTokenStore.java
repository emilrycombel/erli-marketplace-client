package pl.er.shoper.client.auth;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class InMemoryTokenStore implements TokenStore {

    private final AtomicReference<StoredToken> tokenRef = new AtomicReference<>();

    @Override
    public Optional<StoredToken> load() {
        return Optional.ofNullable(tokenRef.get());
    }

    @Override
    public void store(StoredToken token) {
        tokenRef.set(token);
    }

    @Override
    public void clear() {
        tokenRef.set(null);
    }
}
