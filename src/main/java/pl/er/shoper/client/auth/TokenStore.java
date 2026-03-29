package pl.er.shoper.client.auth;

import java.util.Optional;

public interface TokenStore {

    Optional<StoredToken> load();

    void store(StoredToken token);

    void clear();
}
