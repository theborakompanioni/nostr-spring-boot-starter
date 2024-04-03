package org.tbk.nostr.example.relay.impl.nip50;


import com.github.pemistahl.lingua.api.IsoCode639_1;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.example.relay.domain.event.EventEntityService;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class Nip50Config {

    @Bean
    LanguageDetector languageDetector() {
        // NEVER use a language detector with ALL language models:
        // - requires between 3.5 and 4.0 GB of memory
        // - takes quite a while to load
        // For more info see: https://github.com/pemistahl/lingua/issues/191
        return LanguageDetectorBuilder
                .fromIsoCodes639_1(IsoCode639_1.EN, IsoCode639_1.ES, IsoCode639_1.FR)
                .build();
    }

    @Bean
    Nip50EventPostProcessorHandler nip50EventPostProcessorHandler(EventEntityService eventEntityService,
                                                                  LanguageDetector languageDetector) {
        return new Nip50EventPostProcessorHandler(eventEntityService, languageDetector);
    }

    @Bean
    InitializingBean initializeLanguageDetection(LanguageDetector languageDetector) {
        return () -> {
            Stopwatch sw = Stopwatch.createStarted();
            log.debug("Initialize language detection for NIP-50…");
            languageDetector.detectLanguageOf("This request will trigger loading of language models.");
            log.debug("Initializing language detection for NIP-50 took {}", sw.stop());
        };
    }
}
