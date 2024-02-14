package org.tbk.nostr.relay.example.nostr.extension.nip40;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip40ExtensionConfiguration {

    @Bean
    Nip40RequestHandlerInterceptor nip40RequestHandlerInterceptor(Nip40Support support) {
        return new Nip40RequestHandlerInterceptor(support);
    }
}
