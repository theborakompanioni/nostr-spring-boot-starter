package org.tbk.nostr.example.client;

import com.google.common.collect.ImmutableMap;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
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

    @Bean
    WebSocketContainer webSocketContainer() {
        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketContainer.setAsyncSendTimeout(Duration.ofMinutes(1).toMillis());
        webSocketContainer.setDefaultMaxSessionIdleTimeout(Duration.ofMinutes(3).toMillis());
        webSocketContainer.setDefaultMaxTextMessageBufferSize(65_536);
        webSocketContainer.setDefaultMaxBinaryMessageBufferSize(65_536);
        return webSocketContainer;
    }

    @Bean
    WebSocketClient webSocketClient(WebSocketContainer webSocketContainer) {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient(webSocketContainer);
        webSocketClient.setUserProperties(ImmutableMap.<String, Object>builder().build());
        return webSocketClient;
    }

    @Bean(destroyMethod = "shutDown")
    NostrClientService nostrClientService(RelayUri relayUri, WebSocketClient webSocketClient) throws TimeoutException {
        SimpleNostrClientService simpleReactiveNostrClient = new SimpleNostrClientService(relayUri, webSocketClient);
        simpleReactiveNostrClient.startAsync();
        simpleReactiveNostrClient.awaitRunning(Duration.ofSeconds(60));
        return simpleReactiveNostrClient;
    }
}
