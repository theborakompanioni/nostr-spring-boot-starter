package org.tbk.nostr.relay.example.nostr.extension.nip9;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip9ExtensionConfiguration {

    @Bean
    Nip9RequestHandlerInterceptor nip9RequestHandlerInterceptor(Nip9Support support) {
        return new Nip9RequestHandlerInterceptor(support);
    }

    @Bean
    @Order(900_000)
    DeletionEventValidator deletionEventValidator() {
        return new DeletionEventValidator();
    }
}
