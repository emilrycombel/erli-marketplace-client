package pl.erli.client.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class FileTokenStore implements TokenStore {

    private static final Logger log = LogManager.getLogger(FileTokenStore.class);

    private final Path tokenFile;

    public FileTokenStore(Path tokenFile) {
        this.tokenFile = tokenFile.toAbsolutePath();
    }

    @Override
    public Optional<String> load() {
        if (!Files.exists(tokenFile)) {
            return Optional.empty();
        }
        try {
            String content = Files.readString(tokenFile).strip();
            return content.isEmpty() ? Optional.empty() : Optional.of(content);
        } catch (IOException e) {
            log.warn("Failed to read token from {}", tokenFile, e);
            return Optional.empty();
        }
    }

    @Override
    public void save(String token) {
        Path dir = tokenFile.getParent();
        try {
            Files.createDirectories(dir);
            // On POSIX file systems createTempFile creates the file as rw------- (owner only).
            Path tmp = Files.createTempFile(dir, ".erli-token", ".tmp");
            try {
                Files.writeString(tmp, token);
                moveIntoPlace(tmp);
            } finally {
                Files.deleteIfExists(tmp);
            }
            log.debug("Token saved to {}", tokenFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save token to " + tokenFile, e);
        }
    }

    private void moveIntoPlace(Path tmp) throws IOException {
        try {
            Files.move(tmp, tokenFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, tokenFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
