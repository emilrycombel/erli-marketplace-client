package pl.er.shoper.client.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FileTokenStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void storeAndLoadToken() {
        var store = new FileTokenStore(tempDir.resolve("token.json"));
        var token = new StoredToken("abc123", Instant.now().plusSeconds(3600));

        store.store(token);
        var loaded = store.load();

        assertTrue(loaded.isPresent());
        assertEquals("abc123", loaded.get().accessToken());
    }

    @Test
    void loadReturnsEmptyWhenNoFile() {
        var store = new FileTokenStore(tempDir.resolve("nonexistent.json"));
        assertTrue(store.load().isEmpty());
    }

    @Test
    void clearRemovesFile() {
        var store = new FileTokenStore(tempDir.resolve("token.json"));
        var token = new StoredToken("abc123", Instant.now().plusSeconds(3600));

        store.store(token);
        store.clear();

        assertTrue(store.load().isEmpty());
    }

    @Test
    void storedTokenPreservesExpiry() {
        var store = new FileTokenStore(tempDir.resolve("token.json"));
        var expiresAt = Instant.parse("2030-01-01T00:00:00Z");
        var token = new StoredToken("token", expiresAt);

        store.store(token);
        var loaded = store.load().orElseThrow();

        assertEquals(expiresAt, loaded.expiresAt());
    }
}
