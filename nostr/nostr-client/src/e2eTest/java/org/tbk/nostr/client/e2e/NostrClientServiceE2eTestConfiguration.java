package org.tbk.nostr.client.e2e;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.client.SimpleNostrClientService;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration(proxyBeanMethods = false)
class NostrClientServiceE2eTestConfiguration {

    @Bean
    RelayUri relay() {
        return RelayUri.parse("wss://relay.primal.net");
    }

    @Bean(destroyMethod = "shutDown")
    NostrClientService nostrClient(RelayUri relayUri) throws TimeoutException {
        SimpleNostrClientService simpleReactiveNostrClient = new SimpleNostrClientService(relayUri);
        simpleReactiveNostrClient.startAsync();
        simpleReactiveNostrClient.awaitRunning(Duration.ofSeconds(60));
        return simpleReactiveNostrClient;
    }
}
