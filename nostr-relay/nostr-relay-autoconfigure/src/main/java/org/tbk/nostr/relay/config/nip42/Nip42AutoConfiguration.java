package org.tbk.nostr.relay.config.nip42;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.handler.AuthRequestHandler;
import org.tbk.nostr.relay.nip42.Nip42Support;
import org.tbk.nostr.relay.nip42.handler.AuthConnectionEstablishedHandler;
import org.tbk.nostr.relay.nip42.handler.SimpleAuthRequestHandler;
import org.tbk.nostr.relay.nip42.impl.SimpleNip42Support;
import org.tbk.nostr.relay.nip42.interceptor.AuthenticationInterceptor;
import org.tbk.nostr.relay.nip42.interceptor.SimpleAuthenticationInterceptor;

@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@EnableConfigurationProperties(Nip42Properties.class)
@ConditionalOnClass(Nip42Support.class)
@ConditionalOnProperty(value = "org.tbk.nostr.nip42.enabled")
@RequiredArgsConstructor
class Nip42AutoConfiguration {

    @NonNull
    private final Nip42Properties properties;

    @Bean
    @ConditionalOnMissingBean(Nip42Support.class)
    Nip42Support simpleNip42Support() {
        return new SimpleNip42Support();
    }

    @Bean
    AuthConnectionEstablishedHandler authConnectionEstablishedHandler(Nip42Support nip42Support) {
        return new AuthConnectionEstablishedHandler(nip42Support);
    }

    @Bean
    @ConditionalOnMissingBean(AuthRequestHandler.class)
    AuthRequestHandler simpleAuthRequestHandler(Nip42Support nip42Support) {
        return new SimpleAuthRequestHandler(nip42Support);
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationInterceptor.class)
    AuthenticationInterceptor simpleAuthenticationInterceptor(Nip42Support nip42Support) {
        return new SimpleAuthenticationInterceptor(nip42Support);
    }

}
