package pl.erli.client.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcTokenStoreTest {

    private JdbcTokenStore store;

    @BeforeEach
    void setUp() {
        DataSource ds = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .build();
        store = new JdbcTokenStore(ds, "erli_token", "test");
        store.initTable();
    }

    @Test
    void shouldReturnEmptyWhenNoTokenSaved() {
        assertEquals(Optional.empty(), store.load());
    }

    @Test
    void shouldSaveAndLoadToken() {
        store.save("my-secret-token");

        Optional<String> loaded = store.load();
        assertTrue(loaded.isPresent());
        assertEquals("my-secret-token", loaded.get());
    }

    @Test
    void shouldOverwriteExistingToken() {
        store.save("token-v1");
        store.save("token-v2");

        assertEquals("token-v2", store.load().orElseThrow());
    }

    @Test
    void shouldIsolateByStoreKey() {
        DataSource ds = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .build();
        var store1 = new JdbcTokenStore(ds, "erli_token", "shop-a");
        var store2 = new JdbcTokenStore(ds, "erli_token", "shop-b");
        store1.initTable();

        store1.save("token-a");
        store2.save("token-b");

        assertEquals("token-a", store1.load().orElseThrow());
        assertEquals("token-b", store2.load().orElseThrow());
    }
}
