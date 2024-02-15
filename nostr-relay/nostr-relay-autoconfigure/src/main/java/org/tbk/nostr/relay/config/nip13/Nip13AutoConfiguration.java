package org.tbk.nostr.relay.config.nip13;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.nip13.validation.ProofOfWorkEventValidator;

@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@EnableConfigurationProperties(Nip13Properties.class)
@ConditionalOnClass(ProofOfWorkEventValidator.class)
@ConditionalOnProperty(value = "org.tbk.nostr.nip13.enabled")
@RequiredArgsConstructor
class Nip13AutoConfiguration {

    @NonNull
    private final Nip13Properties properties;

    @Bean
    ProofOfWorkEventValidator proofOfWorkEventValidator() {
        return new ProofOfWorkEventValidator(this.properties.getMinPowDifficulty(), this.properties.getRequireCommitment());
    }
}
