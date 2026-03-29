package pl.er.shoper.client.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

public class FileTokenStore implements TokenStore {

    private static final Logger log = LogManager.getLogger(FileTokenStore.class);

    private final Path filePath;
    private final ObjectMapper objectMapper;

    public FileTokenStore(Path filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Optional<StoredToken> load() {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try {
            var node = objectMapper.readTree(filePath.toFile());
            var token = new StoredToken(
                    node.get("accessToken").asText(),
                    Instant.parse(node.get("expiresAt").asText())
            );
            return Optional.of(token);
        } catch (IOException e) {
            log.warn("Failed to load token from file: {}", filePath, e);
            return Optional.empty();
        }
    }

    @Override
    public void store(StoredToken token) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("accessToken", token.accessToken());
            node.put("expiresAt", token.expiresAt().toString());
            objectMapper.writeValue(filePath.toFile(), node);
        } catch (IOException e) {
            log.error("Failed to persist token to file: {}", filePath, e);
        }
    }

    @Override
    public void clear() {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete token file: {}", filePath, e);
        }
    }
}
