package pl.erli.client.auth;

import java.util.Optional;

public interface TokenStore {

    Optional<String> load();

    void save(String token);
}
