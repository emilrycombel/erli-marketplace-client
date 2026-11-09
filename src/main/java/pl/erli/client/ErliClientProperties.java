package pl.erli.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erli.api")
public record ErliClientProperties(
        String baseUrl,
        String token,
        String tokenFile,
        String tokenStore,
        RateLimit rateLimit,
        PaginationConfig pagination,
        JdbcConfig jdbc
) {
    public ErliClientProperties {
        if (baseUrl == null) baseUrl = "https://erli.pl/svc/shop-api";
        if (rateLimit == null) rateLimit = new RateLimit(3, 1000);
        if (pagination == null) pagination = new PaginationConfig(50, 2);
    }

    public record RateLimit(int maxRetries, long initialDelayMs) {
        public RateLimit {
            if (maxRetries < 0) maxRetries = 3;
            if (initialDelayMs <= 0) initialDelayMs = 1000;
        }
    }

    public record PaginationConfig(int pageSize, int prefetchPages) {
        public PaginationConfig {
            if (pageSize <= 0) pageSize = 50;
            if (prefetchPages <= 0) prefetchPages = 2;
        }
    }

    public record JdbcConfig(String tableName, String storeKey, boolean initTable) {
        public JdbcConfig {
            if (tableName == null) tableName = "erli_token";
            if (storeKey == null) storeKey = "default";
        }
    }
}
