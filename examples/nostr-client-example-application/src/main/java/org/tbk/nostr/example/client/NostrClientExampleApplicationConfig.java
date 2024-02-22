package org.tbk.nostr.example.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.client.SimpleNostrClientService;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NostrClientExampleApplicationProperties.class)
@RequiredArgsConstructor
class NostrClientExampleApplicationConfig {

    @NonNull
    private final NostrClientExampleApplicationProperties properties;

    @Bean
    RelayUri relayUri() {
        return RelayUri.of(properties.getRelayUri());
    }

    @Bean(destroyMethod = "shutDown")
    NostrClientService nostrClientService(RelayUri relayUri) throws TimeoutException {
        SimpleNostrClientService simpleReactiveNostrClient = new SimpleNostrClientService(relayUri);
        simpleReactiveNostrClient.startAsync();
        simpleReactiveNostrClient.awaitRunning(Duration.ofSeconds(60));
        return simpleReactiveNostrClient;
    }
}
