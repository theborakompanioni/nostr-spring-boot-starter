package org.tbk.nostr.relay.support;

import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.EventStreamSupport;
import org.tbk.nostr.relay.SubscriptionSupport;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * This class is responsible for bringing {@link SubscriptionSupport} and {@link org.tbk.nostr.relay.EventStreamSupport}
 * together. It is a singleton that initializes the {@code EventStreamSupport} and publishes all incoming
 * event to all clients with matching subscriptions.
 */
@Slf4j
public class EventToSubscribers implements Consumer<Event> {

    private final SubscriptionSupport subscriptionSupport;

    public EventToSubscribers(SubscriptionSupport subscriptionSupport,
                              EventStreamSupport eventStreamSupport) {
        this.subscriptionSupport = requireNonNull(subscriptionSupport);

        eventStreamSupport.init(this);
    }

    @Override
    public void accept(Event event) {
        subscriptionSupport.findMatching(event)
                .filter(it -> it.session().isOpen())
                .subscribe(entry -> {
                    try {
                        entry.session().sendResponseImmediately(Response.newBuilder()
                                .setEvent(EventResponse.newBuilder()
                                        .setSubscriptionId(entry.request().getId())
                                        .setEvent(event)
                                        .build())
                                .build());
                    } catch (Exception e) {
                        log.warn("Error while sending event to session '{}': {}", entry.session().getId(), e.getMessage());
                    }
                });
    }
}
