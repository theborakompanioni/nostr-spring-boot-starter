package org.tbk.nostr.relay.example.extension.nip1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip1Extension {

    @Bean
    ReplaceableEventRequestHandlerInterceptor replaceableEventRequestHandlerInterceptor(Nip1Support support) {
        return new ReplaceableEventRequestHandlerInterceptor(support);
    }
}
