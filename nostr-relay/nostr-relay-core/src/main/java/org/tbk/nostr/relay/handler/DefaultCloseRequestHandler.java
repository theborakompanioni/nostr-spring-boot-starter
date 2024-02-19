package org.tbk.nostr.relay.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.SessionSubscriptionSupport;
import org.tbk.nostr.relay.SessionSubscriptionSupport.SessionId;
import org.tbk.nostr.relay.SessionSubscriptionSupport.SessionSubscription;
import org.tbk.nostr.relay.SessionSubscriptionSupport.SubscriptionKey;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class DefaultCloseRequestHandler implements CloseRequestHandler,
        RequestHandlerInterceptor,
        ConnectionClosedHandler {

    private final SessionSubscriptionSupport support;

    @Override
    public void afterConnectionClosed(NostrWebSocketSession session, CloseStatus closeStatus) {
        support.removeAll(new SessionId(session.getId()));
    }

    @Override
    public void handleCloseMessage(NostrWebSocketSession session, CloseRequest close) {
        log.debug("Closing active subscription '{}' on CLOSE command: {}", close.getId(), session.getId());

        SessionId sessionId = new SessionId(session.getId());
        SubscriptionId subscriptionId = SubscriptionId.of(close.getId());

        SubscriptionKey key = new SubscriptionKey(sessionId, subscriptionId);
        support.remove(key);

        session.queueResponse(Response.newBuilder()
                .setClosed(ClosedResponse.newBuilder()
                        .setSubscriptionId(close.getId())
                        .build())
                .build());
    }

    @Override
    public void postHandle(NostrWebSocketSession session, Request request) {
        if (request.getKindCase() == Request.KindCase.REQ) {
            handleReqMessage(session, request.getReq());
        } else if (request.getKindCase() == Request.KindCase.EVENT) {
            handleEventMessage(request.getEvent());
        }
    }

    private void handleReqMessage(NostrWebSocketSession session, ReqRequest req) {
        log.debug("Creating active subscription '{}' on REQ command: {}", req.getId(), session.getId());

        SessionId sessionId = new SessionId(session.getId());
        SubscriptionId subscriptionId = SubscriptionId.of(req.getId());

        SubscriptionKey key = new SubscriptionKey(sessionId, subscriptionId);
        SessionSubscription sessionSubscription = new SessionSubscription(session, req);

        support.add(key, sessionSubscription);
    }

    private void handleEventMessage(EventRequest event) {
        support.findMatching(event.getEvent()).subscribe(entry -> {
            try {
                entry.session().sendResponseImmediately(Response.newBuilder()
                        .setEvent(EventResponse.newBuilder()
                                .setSubscriptionId(entry.request().getId())
                                .setEvent(event.getEvent())
                                .build()).build());
            } catch (IOException e) {
                log.warn("Could not send event to client matching subscription: {}", e.getMessage());
            }
        });
    }
}
