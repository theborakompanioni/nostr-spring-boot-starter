package org.tbk.nostr.relay.config.nip42;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.config.nip13.Nip13Properties;
import org.tbk.nostr.relay.nip13.validation.ProofOfWorkEventValidator;

@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@EnableConfigurationProperties(Nip42Properties.class)
@ConditionalOnClass(ProofOfWorkEventValidator.class)
@ConditionalOnProperty(value = "org.tbk.nostr.nip42.enabled")
@RequiredArgsConstructor
class Nip42AutoConfiguration {

    @NonNull
    private final Nip13Properties properties;

    @Bean
    ProofOfWorkEventValidator proofOfWorkEventValidator() {
        return new ProofOfWorkEventValidator(this.properties.getMinPowDifficulty(), this.properties.getRequireCommitment());
    }
}
