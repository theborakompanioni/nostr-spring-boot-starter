package org.tbk.nostr.client;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.base.RelayUri;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class NostrClientServiceTestConfiguration {

    @Bean
    RelayUri relayUri() {
        return RelayUri.parse("ws://localhost:7000");
    }

    @Bean(destroyMethod = "shutDown")
    NostrClientService nostrClient(RelayUri relayUri) throws TimeoutException {
        SimpleNostrClientService simpleReactiveNostrClient = new SimpleNostrClientService(relayUri);
        simpleReactiveNostrClient.startAsync();
        simpleReactiveNostrClient.awaitRunning(Duration.ofSeconds(60));
        return simpleReactiveNostrClient;
    }
}
