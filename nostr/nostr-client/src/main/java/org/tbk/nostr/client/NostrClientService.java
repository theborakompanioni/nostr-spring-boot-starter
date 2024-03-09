package org.tbk.nostr.client;

import lombok.Builder;
import lombok.Value;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.ReqRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface NostrClientService {
    default Flux<Event> subscribe(ReqRequest req) {
        return this.subscribe(req, SubscribeOptions.defaultOptions());
    }

    Flux<Event> subscribe(ReqRequest req, SubscribeOptions options);

    Flux<Event> attachTo(SubscriptionId id);

    Mono<Void> close(SubscriptionId id);

    Mono<Void> send(Event event);

    boolean isConnected();

    Mono<Boolean> reconnect(Duration delay);

    @Value
    @Builder(toBuilder = true)
    class SubscribeOptions {
        public static SubscribeOptions defaultOptions() {
            return SubscribeOptions.builder().build();
        }

        @Builder.Default
        boolean closeOnEndOfStream = true;
    }
}
