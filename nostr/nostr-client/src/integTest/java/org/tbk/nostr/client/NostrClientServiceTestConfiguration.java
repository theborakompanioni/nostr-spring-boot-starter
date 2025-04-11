package org.tbk.nostr.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.base.RelayUri;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration(proxyBeanMethods = false)
class NostrClientServiceTestConfiguration {

    @Bean
    RelayUri relayUri() {
        return RelayUri.parse("ws://localhost:7000");
    }

    @Bean(destroyMethod = "shutDown")
    NostrClientService nostrClient(RelayUri relayUri) throws TimeoutException {
        SimpleNostrClientService nostrClient = new SimpleNostrClientService(relayUri);
        nostrClient.startAsync();
        nostrClient.awaitRunning(Duration.ofSeconds(60));
        return nostrClient;
    }
}
