package org.tbk.nostr.relay.support;

import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.EventStreamSupport;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleEventStreamSupport implements EventStreamSupport, RequestHandlerInterceptor {
    private Consumer<Event> eventConsumer;

    @Override
    public void init(Consumer<Event> eventConsumer) {
        this.eventConsumer = requireNonNull(eventConsumer);
    }

    @Override
    public void postHandle(NostrRequestContext context, Request request) {
        if (context.getHandledEvent().isPresent()) {
            eventConsumer.accept(context.getHandledEvent().get());
        }
    }
}
