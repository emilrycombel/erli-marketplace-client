package pl.erli.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "erli.api")
public record ErliClientProperties(
        @DefaultValue("https://erli.pl/svc/shop-api") String baseUrl,
        String token,
        String tokenFile,
        String tokenStore,
        @DefaultValue("10s") Duration connectTimeout,
        @DefaultValue("30s") Duration readTimeout,
        @DefaultValue RateLimit rateLimit,
        @DefaultValue PaginationConfig pagination,
        @DefaultValue JdbcConfig jdbc
) {
    public record RateLimit(
            @DefaultValue("3") int maxRetries,
            @DefaultValue("1000") long initialDelayMs
    ) {}

    public record PaginationConfig(
            @DefaultValue("50") int pageSize,
            @DefaultValue("2") int prefetchPages
    ) {}

    public record JdbcConfig(
            @DefaultValue("erli_token") String tableName,
            @DefaultValue("default") String storeKey,
            @DefaultValue("true") boolean initTable
    ) {}
}
