package pl.erli.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import pl.erli.client.auth.PersistedTokenProvider;
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
