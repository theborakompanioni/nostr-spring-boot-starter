package org.tbk.nostr.relay.example.extension.nip9;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip9Extension {

    @Bean
    Nip9RequestHandlerInterceptor nip9RequestHandlerInterceptor(Nip9Support support) {
        return new Nip9RequestHandlerInterceptor(support);
    }
}
