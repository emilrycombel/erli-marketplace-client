package pl.erli.client.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcTokenStore implements TokenStore {

    private static final Logger log = LogManager.getLogger(JdbcTokenStore.class);

    private final DataSource dataSource;
    private final String tableName;
    private final String storeKey;

    public JdbcTokenStore(DataSource dataSource) {
        this(dataSource, "erli_token", "default");
    }

    public JdbcTokenStore(DataSource dataSource, String tableName, String storeKey) {
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.storeKey = storeKey;
    }

    public void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "store_key VARCHAR(255) PRIMARY KEY, "
                + "token TEXT NOT NULL, "
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            log.debug("Token table '{}' initialized", tableName);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize token table: " + tableName, e);
        }
    }

    @Override
    public Optional<String> load() {
        String sql = "SELECT token FROM " + tableName + " WHERE store_key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, storeKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("token"));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.warn("Failed to load token for key '{}' from table '{}'", storeKey, tableName, e);
            return Optional.empty();
        }
    }

    @Override
    public void save(String token) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int updated = executeUpdate(conn,
                        "UPDATE " + tableName + " SET token = ?, updated_at = CURRENT_TIMESTAMP WHERE store_key = ?",
                        token, storeKey);
                if (updated == 0) {
                    executeUpdate(conn,
                            "INSERT INTO " + tableName + " (store_key, token, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                            storeKey, token);
                }
                conn.commit();
                log.debug("Token saved for key '{}' in table '{}'", storeKey, tableName);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save token for key '" + storeKey + "'", e);
        }
    }

    private int executeUpdate(Connection conn, String sql, String... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }
}
