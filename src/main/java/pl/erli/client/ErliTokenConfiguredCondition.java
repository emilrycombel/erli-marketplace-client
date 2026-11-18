package pl.erli.client;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import pl.erli.client.auth.TokenStore;

class ErliTokenConfiguredCondition extends AnyNestedCondition {

    ErliTokenConfiguredCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnProperty(prefix = "erli.api", name = "token")
    static class Token {}

    @ConditionalOnProperty(prefix = "erli.api", name = "token-file")
    static class TokenFile {}

    @ConditionalOnProperty(prefix = "erli.api", name = "token-store", havingValue = "jdbc")
    static class Jdbc {}

    @ConditionalOnBean(TokenStore.class)
    static class Store {}
}
