package org.tbk.nostr.example.agentic;

import com.google.common.collect.ImmutableMap;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.client.SimpleNostrClientService;
import org.tbk.nostr.example.agentic.NostrAgenticExampleApplicationProperties.IdentityProperties;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.ProfileMetadata;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NostrAgenticExampleApplicationProperties.class)
@RequiredArgsConstructor
class NostrAgenticExampleApplicationConfig {

    @NonNull
    private final NostrAgenticExampleApplicationProperties properties;

    @Bean
    Identity nostrIdentity() {
        return properties.getIdentity()
                .map(IdentityProperties::getSeed)
                .map(MoreIdentities::fromSeed)
                .orElseThrow(() -> new IllegalStateException("Could not create nostr identity from mnemonic."));
    }

    @Bean
    ProfileMetadata nostrProfileMetadata() {
        return properties.getProfileMetadata()
                .map(NostrAgenticExampleApplicationProperties.ProfileMetadataProperties::toProfileMetadata)
                .orElseThrow(() -> new IllegalStateException("Could not create nostr profile-metadata from properties."));
    }

    @Bean
    @ConditionalOnBean(Identity.class)
    Signer nostrSigner(Identity nostrIdentity) {
        return SimpleSigner.fromIdentity(nostrIdentity);
    }

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

    @Bean(destroyMethod = "stopAsync")
    NostrClientService nostrClientService(RelayUri relayUri, WebSocketClient webSocketClient) throws TimeoutException {
        SimpleNostrClientService simpleReactiveNostrClient = new SimpleNostrClientService(relayUri, webSocketClient);
        simpleReactiveNostrClient.startAsync();
        simpleReactiveNostrClient.awaitRunning(Duration.ofSeconds(60));
        return simpleReactiveNostrClient;
    }

    @Bean
    NostrTemplate nostrTemplate(RelayUri relayUri) {
        return new SimpleNostrTemplate(relayUri);
    }

    @Bean
    @ConditionalOnBean({NostrTemplate.class, Signer.class, ProfileMetadata.class})
    NostrProfileMetadataUpdateRunner profileMetadataUpdateRunner(NostrTemplate nostrTemplate,
                                                                 Signer signer,
                                                                 ProfileMetadata profileMetadata) {
        return new NostrProfileMetadataUpdateRunner(nostrTemplate, signer, profileMetadata);
    }
}
