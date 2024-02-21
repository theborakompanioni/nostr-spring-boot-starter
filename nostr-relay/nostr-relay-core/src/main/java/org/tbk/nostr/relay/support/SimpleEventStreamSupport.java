package org.tbk.nostr.relay.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.EventResponse;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.EventStreamSupport;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.SubscriptionSupport;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class SimpleEventStreamSupport implements EventStreamSupport, RequestHandlerInterceptor {

    private final SubscriptionSupport support;

    @Override
    public void postHandle(NostrWebSocketSession session, Request request) {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            handleEventMessage(request.getEvent());
        }
    }

    private void handleEventMessage(EventRequest event) {
        support.findMatching(event.getEvent())
                .filter(it -> it.session().isOpen())
                .subscribe(entry -> {
                    try {
                        entry.session().sendResponseImmediately(Response.newBuilder()
                                .setEvent(EventResponse.newBuilder()
                                        .setSubscriptionId(entry.request().getId())
                                        .setEvent(event.getEvent())
                                        .build())
                                .build());
                    } catch (Exception e) {
                        log.warn("Error while sending event to session '{}': {}", entry.session().getId(), e.getMessage());
                    }
                });
    }
}
