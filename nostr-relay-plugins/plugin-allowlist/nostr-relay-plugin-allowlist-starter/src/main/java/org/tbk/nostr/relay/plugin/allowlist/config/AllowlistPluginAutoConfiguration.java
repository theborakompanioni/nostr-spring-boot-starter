package org.tbk.nostr.relay.plugin.allowlist.config;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.plugin.allowlist.Allowlist;
import org.tbk.nostr.relay.plugin.allowlist.EmptyAllowlist;
import org.tbk.nostr.relay.plugin.allowlist.StaticAllowlist;
import org.tbk.nostr.relay.plugin.allowlist.validation.AllowlistValidator;

import java.util.List;


@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@EnableConfigurationProperties(AllowlistPluginProperties.class)
@ConditionalOnClass(AllowlistValidator.class)
@ConditionalOnProperty(value = "org.tbk.nostr.plugin.allowlist.enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class AllowlistPluginAutoConfiguration {

    @NonNull
    private final AllowlistPluginProperties properties;

    @Bean
    @ConditionalOnMissingBean
    Allowlist allowlist() {
        List<XonlyPublicKey> allowed = properties.getAllowed().stream()
                .toList();

        return allowed.isEmpty() ? new EmptyAllowlist() : new StaticAllowlist(allowed);
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    AllowlistValidator allowlistValidator(Allowlist allowlist) {
        return new AllowlistValidator(allowlist);
    }
}
