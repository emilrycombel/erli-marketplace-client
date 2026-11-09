package pl.erli.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.erli.client.auth.*;
import pl.erli.client.http.ErliRestClientFactory;

import java.nio.file.Path;
import java.util.Optional;

@AutoConfiguration
@EnableConfigurationProperties(ErliClientProperties.class)
public class ErliAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "javax.sql.DataSource")
    @ConditionalOnBean(javax.sql.DataSource.class)
    @ConditionalOnProperty(prefix = "erli.api", name = "token-store", havingValue = "jdbc")
    static class JdbcTokenStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(TokenStore.class)
        public TokenStore erliJdbcTokenStore(javax.sql.DataSource dataSource,
                                              ErliClientProperties properties) {
            String table = properties.jdbc() != null && properties.jdbc().tableName() != null
                    ? properties.jdbc().tableName() : "erli_token";
            String key = properties.jdbc() != null && properties.jdbc().storeKey() != null
                    ? properties.jdbc().storeKey() : "default";
            var store = new JdbcTokenStore(dataSource, table, key);
            if (properties.jdbc() == null || properties.jdbc().initTable()) {
                store.initTable();
            }
            return store;
        }
    }

    @Bean
    @ConditionalOnMissingBean
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
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TokenProvider.class)
    public ErliClient erliClient(ErliClientProperties properties, TokenProvider tokenProvider) {
        var restClient = ErliRestClientFactory.create(
                properties.baseUrl(),
                tokenProvider,
                properties.rateLimit().maxRetries(),
                properties.rateLimit().initialDelayMs()
        );
        return new ErliClient(
                restClient,
                properties.pagination().pageSize(),
                properties.pagination().prefetchPages()
        );
    }
}
