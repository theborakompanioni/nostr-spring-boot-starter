package org.tbk.nostr.example.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.client.NostrClientService.SubscribeOptions;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.util.MoreSubscriptionIds;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
public class NostrClientExampleApplication {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrClientExampleApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.NONE)
                .run(args);
    }

    private static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    private static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    ApplicationRunner allTextNotesSinceNow(NostrClientService nostrClientService) {
        return args -> {
            SubscriptionId subscriptionId = MoreSubscriptionIds.random();

            ReqRequest reqRequest = ReqRequest.newBuilder()
                    .setId(subscriptionId.getId())
                    .addFilters(Filter.newBuilder()
                            .addKinds(1)
                            .setSince(Instant.now().minusSeconds(21).getEpochSecond())
                            .build())
                    .build();

            log.info("[ALL] Will subscribe (subscription_id = '{}')…", subscriptionId.getId());
            nostrClientService.subscribe(reqRequest, SubscribeOptions.defaultOptions().toBuilder()
                            .closeOnEndOfStream(false)
                            .build())
                    .doOnSubscribe(foo -> {
                        log.info("[ALL] Subscribed.");
                    })
                    .delaySubscription(Duration.ofSeconds(5))
                    .subscribe(it -> {
                        log.info("[ALL] New event: '{}'", it.getContent());
                    }, e -> {
                        log.error("[ALL] Error during event stream: {}", e.getMessage());
                    }, () -> {
                        log.info("[ALL] Completed.");
                    });
        };
    }
}
