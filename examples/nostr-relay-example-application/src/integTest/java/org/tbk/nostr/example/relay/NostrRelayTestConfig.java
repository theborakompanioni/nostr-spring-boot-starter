package org.tbk.nostr.example.relay;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.client.SimpleNostrClientService;
import org.tbk.nostr.template.SimpleNostrTemplate;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Lazy // needed for @LocalServerPort to be populated
@TestConfiguration(proxyBeanMethods = false)
class NostrRelayTestConfig {

    private final int serverPort;

    NostrRelayTestConfig(@LocalServerPort int serverPort) {
        this.serverPort = serverPort;
    }

    @Bean
    RelayUri relayUri() {
        return RelayUri.of("ws://localhost:%d".formatted(serverPort));
    }

    @Bean
    SimpleNostrTemplate nostrTemplate(RelayUri relayUri) {
        return new SimpleNostrTemplate(relayUri);
    }

    @Bean(destroyMethod = "stopAsync")
    SimpleNostrClientService nostrClientService(RelayUri relayUri) throws TimeoutException {
        SimpleNostrClientService client = new SimpleNostrClientService(relayUri);
        client.startAsync();
        client.awaitRunning(Duration.ofSeconds(60));
        return client;
    }
}
