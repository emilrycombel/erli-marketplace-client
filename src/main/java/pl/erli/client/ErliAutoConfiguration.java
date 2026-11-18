package pl.erli.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import pl.erli.client.auth.*;
import pl.erli.client.http.ErliRestClientFactory;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Optional;

@AutoConfiguration(afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
@EnableConfigurationProperties(ErliClientProperties.class)
public class ErliAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(prefix = "erli.api", name = "token-store", havingValue = "jdbc")
    static class JdbcTokenStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(TokenStore.class)
        public TokenStore erliJdbcTokenStore(DataSource dataSource, ErliClientProperties properties) {
            var jdbc = properties.jdbc();
            var store = new JdbcTokenStore(dataSource, jdbc.tableName(), jdbc.storeKey());
            if (jdbc.initTable()) {
                store.initTable();
            }
            return store;
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(ErliTokenConfiguredCondition.class)
    public TokenProvider erliTokenProvider(ErliClientProperties properties,
                                           Optional<TokenStore> tokenStore) {
        if (tokenStore.isPresent()) {
            return new PersistedTokenProvider(tokenStore.get());
        }
        if (properties.tokenFile() != null && !properties.tokenFile().isBlank()) {
            return new PersistedTokenProvider(new FileTokenStore(Path.of(properties.tokenFile())));
        }
        if (properties.token() != null && !properties.token().isBlank()) {
            return new StaticTokenProvider(properties.token());
        }
        throw new IllegalStateException(
                "erli.api.token-store=jdbc requires a DataSource bean, or set erli.api.token / erli.api.token-file");
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TokenProvider.class)
    public ErliClient erliClient(ErliClientProperties properties, TokenProvider tokenProvider) {
        var restClient = ErliRestClientFactory.create(
                properties.baseUrl(),
                tokenProvider,
                properties.rateLimit().maxRetries(),
                properties.rateLimit().initialDelayMs(),
                properties.connectTimeout(),
                properties.readTimeout()
        );
        return new ErliClient(
                restClient,
                properties.pagination().pageSize(),
                properties.pagination().prefetchPages()
        );
    }
}
