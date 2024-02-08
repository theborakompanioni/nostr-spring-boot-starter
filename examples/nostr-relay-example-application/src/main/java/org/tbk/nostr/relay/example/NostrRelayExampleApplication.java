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
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Supplier;

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
    ApplicationRunner insertStartupEvents(NostrRelayExampleApplicationProperties properties,
                                          Signer serverSigner,
                                          EventEntityService eventEntityService) {
        Instant now = Instant.now();
        return args -> {
            if (!properties.isStartupEventsEnabled()) {
                log.trace("Skip inserting startup events: Disabled.");
                return;
            }

            log.info("Inserting startup events...");

            Event bootingEvent = MoreEvents.createFinalizedTextNote(serverSigner, "Booting...");
            eventEntityService.createEvent(bootingEvent);

            Supplier<Duration> startupDuration = () -> Duration.ofMillis(Instant.now().toEpochMilli() - now.toEpochMilli());
            Event bootedEvent = MoreEvents.finalize(serverSigner, Nip1.createTextNote(serverSigner.getPublicKey(), "Booted.")
                    .addTags(MoreTags.e(EventId.of(bootingEvent.getId().toByteArray())))
                    .addTags(MoreTags.named("alt", "Took %s".formatted(startupDuration.get())))
            );
            eventEntityService.createEvent(bootedEvent);
        };
    }
}
