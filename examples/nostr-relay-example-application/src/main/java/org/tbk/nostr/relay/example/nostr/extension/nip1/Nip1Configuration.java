package org.tbk.nostr.relay.example.nostr.extension.nip1;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.tbk.nostr.relay.example.nostr.NostrRelayProperties;
import org.tbk.nostr.relay.example.nostr.NostrRequestHandlerExecutionChain;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketHandler;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketHandlerDispatcher;
import org.tbk.nostr.relay.example.nostr.interceptor.MaxFilterCountReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.MaxLimitPerFilterReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.NostrRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.ValidatingEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.validating.CreatedAtLimitEventValidator;
import org.tbk.nostr.relay.example.nostr.validating.DefaultEventValidator;
import org.tbk.nostr.relay.example.nostr.validating.EventValidator;

import java.util.List;

@EnableWebSocket
@ConditionalOnWebApplication
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip1Configuration {

    @Bean
    @Order(1)
    ReplaceableEventValidator replaceableEventValidator() {
        return new ReplaceableEventValidator();
    }

}
