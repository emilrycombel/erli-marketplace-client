package pl.erli.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import pl.erli.client.auth.JdbcTokenStore;
import pl.erli.client.auth.PersistedTokenProvider;

import javax.sql.DataSource;
import pl.erli.client.auth.TokenProvider;
import pl.erli.client.auth.TokenStore;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ErliAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ErliAutoConfiguration.class));

    @Test
    void startsWithoutErliConfiguration() {
        runner.run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx).doesNotHaveBean(ErliClient.class);
        });
    }

    @Test
    void createsClientFromTokenProperty() {
        runner.withPropertyValues("erli.api.token=abc").run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx).hasSingleBean(ErliClient.class);
        });
    }

    @Test
    void startsWithoutClientWhenTokenPropertiesAreBlank() {
        runner.withPropertyValues("erli.api.token=", "erli.api.token-file= ").run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx).doesNotHaveBean(ErliClient.class);
        });
    }

    @Test
    void keepsDefaultRetriesWhenOnlyDelayIsSet() {
        runner.withPropertyValues("erli.api.token=abc", "erli.api.rate-limit.initial-delay-ms=5").run(ctx -> {
            var properties = ctx.getBean(ErliClientProperties.class);
            assertThat(properties.rateLimit().maxRetries()).isEqualTo(3);
            assertThat(properties.rateLimit().initialDelayMs()).isEqualTo(5);
        });
    }

    @Test
    void jdbcStoreCreatesTableWhenOnlyStoreKeyIsSet() {
        runner.withBean(DataSource.class, () -> new EmbeddedDatabaseBuilder()
                        .setType(EmbeddedDatabaseType.H2).generateUniqueName(true).build())
                .withPropertyValues("erli.api.token-store=jdbc", "erli.api.jdbc.store-key=shop-a")
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).hasSingleBean(ErliClient.class);
                    var store = ctx.getBean(TokenStore.class);
                    assertThat(store).isInstanceOf(JdbcTokenStore.class);
                    store.save("from-db");
                    assertThat(ctx.getBean(TokenProvider.class).getToken()).isEqualTo("from-db");
                });
    }

    @Test
    void jdbcStoreUsesSpringBootsAutoConfiguredDataSource() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ErliAutoConfiguration.class, DataSourceAutoConfiguration.class))
                .withPropertyValues("erli.api.token-store=jdbc", "spring.datasource.generate-unique-name=true")
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).hasSingleBean(JdbcTokenStore.class);
                    assertThat(ctx).hasSingleBean(ErliClient.class);
                });
    }

    @Test
    void prefersUserProvidedTokenStore() {
        runner.withBean(TokenStore.class, () -> new TokenStore() {
                    public Optional<String> load() { return Optional.of("from-store"); }
                    public void save(String token) {}
                })
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).hasSingleBean(ErliClient.class);
                    assertThat(ctx.getBean(TokenProvider.class)).isInstanceOf(PersistedTokenProvider.class);
                    assertThat(ctx.getBean(TokenProvider.class).getToken()).isEqualTo("from-store");
                });
    }
}
