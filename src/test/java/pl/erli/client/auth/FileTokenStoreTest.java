package pl.erli.client.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FileTokenStoreTest {

    @TempDir
    Path dir;

    @Test
    void savesAndOverwritesToken() {
        var store = new FileTokenStore(dir.resolve("nested/token.txt"));

        store.save("token-v1");
        store.save("token-v2");

        assertEquals("token-v2", store.load().orElseThrow());
    }

    @Test
    void writesTokenReadableOnlyByOwner() throws Exception {
        assumeTrue(dir.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path file = dir.resolve("token.txt");
        Files.writeString(file, "old");
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-r--r--"));

        new FileTokenStore(file).save("secret");

        assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(file)));
    }

    @Test
    void leavesNoTemporaryFilesBehind() throws Exception {
        new FileTokenStore(dir.resolve("token.txt")).save("secret");

        try (var files = Files.list(dir)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void providerRejectsBlankTokenAndKeepsTheOldOne() {
        var provider = new PersistedTokenProvider(new FileTokenStore(dir.resolve("token.txt")));
        provider.saveToken("good");

        assertThrows(IllegalArgumentException.class, () -> provider.saveToken(" "));
        assertEquals("good", provider.getToken());
    }

    @Test
    void providerKeepsOldTokenWhenSaveFails() {
        var failing = new TokenStore() {
            public java.util.Optional<String> load() { return java.util.Optional.of("old"); }
            public void save(String token) { throw new IllegalStateException("disk full"); }
        };
        var provider = new PersistedTokenProvider(failing);
        assertEquals("old", provider.getToken());

        assertThrows(IllegalStateException.class, () -> provider.saveToken("new"));
        assertEquals("old", provider.getToken());
    }
}
