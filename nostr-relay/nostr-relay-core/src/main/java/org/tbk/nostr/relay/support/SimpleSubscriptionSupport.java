package org.tbk.nostr.relay.support;

import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.SubscriptionSupport;
import org.tbk.nostr.util.MoreFilters;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
public class SimpleSubscriptionSupport implements SubscriptionSupport {

    private final Map<SubscriptionKey, SessionSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<NostrWebSocketSession.SessionId, List<SubscriptionKey>> sessionIdToSubscriptionKeys = new ConcurrentHashMap<>();

    @Override
    public void removeAll(NostrWebSocketSession.SessionId sessionId) {
        List<SubscriptionKey> removed = sessionIdToSubscriptionKeys.remove(sessionId);
        if (removed != null) {
            log.debug("Removing {} active subscriptions after websocket closed: {}", removed.size(), sessionId);
            removed.forEach(subscriptions::remove);
        }
    }

    @Override
    public void remove(SubscriptionKey key) {
        subscriptions.remove(key);
        sessionIdToSubscriptionKeys.computeIfPresent(key.sessionId(), (foo, oldValue) -> {
            List<SubscriptionKey> activeKeys = oldValue.stream()
                    .filter(it -> !it.equals(key))
                    .toList();
            return !activeKeys.isEmpty() ? activeKeys : null;
        });
    }

    @Override
    public void add(SubscriptionKey key, SessionSubscription subscription) {
        subscriptions.put(key, subscription);
        sessionIdToSubscriptionKeys.compute(key.sessionId(), (foo, oldValue) ->
                oldValue == null ? List.of(key) : Stream.concat(oldValue.stream(), Stream.of(key)).toList());
    }

    @Override
    public Flux<SessionSubscription> findMatching(Event event) {
        return Flux.defer(() -> Flux.fromIterable(subscriptions.values())
                .filter(it -> MoreFilters.matches(event, it.request().getFiltersList())));
    }
}
