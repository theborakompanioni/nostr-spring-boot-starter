package org.tbk.nostr.relay.support;

import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.EventStreamSupport;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleEventStreamSupport implements EventStreamSupport, RequestHandlerInterceptor {
    private Consumer<Event> eventConsumer;

    @Override
    public void postHandle(NostrWebSocketSession session, Request request) {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            eventConsumer.accept(request.getEvent().getEvent());
        }
    }

    @Override
    public void init(Consumer<Event> eventConsumer) {
        this.eventConsumer = requireNonNull(eventConsumer);
    }
}
