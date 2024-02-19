package org.tbk.nostr.relay.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.relay.SessionSubscriptionSupport;
import org.tbk.nostr.util.MoreTags;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class InMemorySessionSubscriptionSupport implements SessionSubscriptionSupport {

    private final Map<SubscriptionKey, SessionSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<SessionId, List<SubscriptionKey>> sessionIdToSubscriptionKeys = new ConcurrentHashMap<>();

    @Override
    public void removeAll(SessionId sessionId) {
        List<SubscriptionKey> removed = sessionIdToSubscriptionKeys.remove(sessionId);
        if (removed != null) {
            log.debug("Removing {} active subscriptions after websocket closed: {}", removed.size(), sessionId);
            removed.forEach(subscriptions::remove);
        }
    }

    @Override
    public void remove(SubscriptionKey key) {
        subscriptions.remove(key);
        sessionIdToSubscriptionKeys.computeIfPresent(key.sessionId(), (foo, oldValue) -> oldValue.stream()
                .filter(it -> !it.equals(key))
                .toList());
    }

    @Override
    public void add(SubscriptionKey key, SessionSubscription subscription) {
        subscriptions.put(key, subscription);
        sessionIdToSubscriptionKeys.compute(key.sessionId(), (foo, oldValue) ->
                oldValue == null ? List.of(key) : Stream.concat(oldValue.stream(), Stream.of(key)).toList());
    }

    @Override
    public Flux<SessionSubscription> findMatching(Event event) {
        return Flux.fromStream(() -> subscriptions.values().stream()
                .filter(it -> matchesFilters(event, it.request().getFiltersList())));
    }

    private boolean matchesFilters(Event event, List<Filter> filtersList) {
        return filtersList.stream().anyMatch(it -> matchesFilter(event, it));
    }

    private boolean matchesFilter(Event event, Filter filter) {
        if (filter.getKindsCount() > 0 && !filter.getKindsList().contains(event.getKind())) {
            return false;
        }
        if (filter.getSince() > 0 && event.getCreatedAt() < filter.getSince()) {
            return false;
        }
        if (filter.getUntil() > 0 && event.getCreatedAt() > filter.getUntil()) {
            return false;
        }
        if (filter.getIdsCount() > 0 && !filter.getIdsList().contains(event.getId())) {
            return false;
        }
        if (filter.getAuthorsCount() > 0 && !filter.getAuthorsList().contains(event.getPubkey())) {
            return false;
        }
        if (filter.getTagsCount() > 0) {
            boolean found = false;
            for (TagValue filterTag : filter.getTagsList()) {
                List<TagValue> eventTags = MoreTags.findByName(event, filterTag.getName());
                Optional<String> any = eventTags.stream()
                        .filter(it -> it.getValuesCount() > 0)
                        .map(it -> it.getValues(0))
                        .filter(it -> filterTag.getValuesList().contains(it))
                        .findAny();

                if (any.isPresent()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }
}
