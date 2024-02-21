package org.tbk.nostr.relay;

import lombok.NonNull;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.ReqRequest;
import reactor.core.publisher.Flux;

public interface SubscriptionSupport {
    void removeAll(NostrWebSocketSession.SessionId sessionId);

    void remove(SubscriptionKey key);

    void add(SubscriptionKey key, SessionSubscription subscription);

    Flux<SessionSubscription> findMatching(Event event);

    record SubscriptionKey(@NonNull NostrWebSocketSession.SessionId sessionId,
                           @NonNull SubscriptionId subscriptionId) {
    }


    record SessionSubscription(@NonNull NostrWebSocketSession session,
                               @NonNull ReqRequest request) {
    }

}
