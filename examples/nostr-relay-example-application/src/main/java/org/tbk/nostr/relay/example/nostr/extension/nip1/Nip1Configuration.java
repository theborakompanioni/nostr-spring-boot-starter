package org.tbk.nostr.relay.example.nostr.extension.nip1;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

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
