package org.tbk.nostr.example;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.base.util.MoreSubscriptionIds;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.client.SimpleNostrClientService;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;

@Slf4j
@SpringBootApplication
public class NostrExampleApplication {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    // npub1qny3tkh0acurzla8x3zy4nhrjz5zd8l9sy9jys09umwng00manysew95gx
    private static final String ODELL_PUBKEY = "04c915daefee38317fa734444acee390a8269fe5810b2241e5e6dd343dfbecc9";

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrExampleApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.NONE)
                .run(args);
    }

    public static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    public static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    @Bean
    @Profile("!test")
    ApplicationRunner odellEvents(NostrClientService reactiveNostrRelayClient) {
        return args -> {
            SubscriptionId subscriptionId = MoreSubscriptionIds.random();

            ReqRequest reqRequest = ReqRequest.newBuilder()
                    .setId(subscriptionId.getId())
                    .addFilters(Filter.newBuilder()
                            .addKinds(1)
                            .addAuthors(ByteString.fromHex(ODELL_PUBKEY))
                            .build())
                    .build();

            log.info("[ODELL] WILL SUBSCRIBE (subscription_id = {})...", subscriptionId);

            reactiveNostrRelayClient.subscribe(reqRequest)
                    .doOnSubscribe(foo -> {
                        log.info("[ODELL] SUBSCRIBED");
                    })
                    .subscribe(it -> {
                        log.info("[ODELL] NEW EVENT: {}", it.getContent());
                    });
        };
    }

    @Bean
    @Profile("!test")
    ApplicationRunner allTextNotesSinceNow(NostrClientService reactiveNostrRelayClient) {
        return args -> {
            SubscriptionId subscriptionId = MoreSubscriptionIds.random();

            ReqRequest reqRequest = ReqRequest.newBuilder()
                    .setId(subscriptionId.getId())
                    .addFilters(Filter.newBuilder()
                            .addKinds(1)
                            .setSince(Instant.now().minusSeconds(21).getEpochSecond())
                            .build())
                    .build();

            log.info("[ALL] Will subscribe (subscription_id = {})...", subscriptionId);
            reactiveNostrRelayClient.subscribe(reqRequest, NostrClientService.SubscribeOptions.defaultOptions().toBuilder()
                            .closeOnEndOfStream(false)
                            .build())
                    .delaySubscription(Duration.ofSeconds(5))
                    .doOnSubscribe(foo -> {
                        log.info("[ALL] Subscribed");
                    })
                    .subscribe(it -> {
                        log.info("[ALL] New event: {}", it.getContent());
                    });
        };
    }
}
