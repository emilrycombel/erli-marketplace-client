package pl.erli.client;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;
import pl.erli.client.auth.TokenStore;

class ErliTokenConfiguredCondition extends AnyNestedCondition {

    ErliTokenConfiguredCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @Conditional(CredentialPropertySet.class)
    static class Credential {}

    @ConditionalOnProperty(prefix = "erli.api", name = "token-store", havingValue = "jdbc")
    static class Jdbc {}

    @ConditionalOnBean(TokenStore.class)
    static class Store {}

    static class CredentialPropertySet implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            var env = context.getEnvironment();
            return StringUtils.hasText(env.getProperty("erli.api.token"))
                    || StringUtils.hasText(env.getProperty("erli.api.token-file"));
        }
    }
}
