package org.tbk.nostr.relay.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
public class NostrRelayExampleApplication {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrRelayExampleApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    ApplicationRunner insertApplicationStartupEvent(EventEntityService eventEntityService) {
        return args -> {
            Signer signer = SimpleSigner.random();
            Event bootingEvent = MoreEvents.createFinalizedTextMessage(signer, "Booting...");
            eventEntityService.createEvent(bootingEvent);

            Event bootedEvent = MoreEvents.finalize(signer, MoreEvents.createTextMessage(signer, "Booted.")
                    .addTags(MoreTags.e(EventId.of(bootingEvent.getId().toByteArray())))
            );
            eventEntityService.createEvent(bootedEvent);
        };
    }
}
