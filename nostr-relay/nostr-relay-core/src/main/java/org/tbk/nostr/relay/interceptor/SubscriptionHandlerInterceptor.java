package org.tbk.nostr.relay.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.proto.CloseRequest;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.SubscriptionSupport;
import org.tbk.nostr.relay.SubscriptionSupport.SessionId;
import org.tbk.nostr.relay.SubscriptionSupport.SessionSubscription;
import org.tbk.nostr.relay.SubscriptionSupport.SubscriptionKey;
import org.tbk.nostr.relay.handler.ConnectionClosedHandler;

@Slf4j
@RequiredArgsConstructor
public class SubscriptionHandlerInterceptor implements RequestHandlerInterceptor, ConnectionClosedHandler {

    private final SubscriptionSupport support;

    @Override
    public void afterConnectionClosed(NostrWebSocketSession session, CloseStatus closeStatus) {
        support.removeAll(new SessionId(session.getId()));
    }

    @Override
    public void postHandle(NostrWebSocketSession session, Request request) {
        if (request.getKindCase() == Request.KindCase.REQ) {
            handleReqMessage(session, request.getReq());
        } else if (request.getKindCase() == Request.KindCase.CLOSE) {
            handleCloseMessage(session, request.getClose());
        }
    }

    private void handleReqMessage(NostrWebSocketSession session, ReqRequest req) {
        if (log.isDebugEnabled()) {
            log.debug("Creating subscription '{}' on REQ command for session '{}'", req.getId(), session.getId());
        }

        SessionId sessionId = new SessionId(session.getId());
        SubscriptionId subscriptionId = SubscriptionId.of(req.getId());

        SubscriptionKey key = new SubscriptionKey(sessionId, subscriptionId);
        SessionSubscription sessionSubscription = new SessionSubscription(session, req);

        support.add(key, sessionSubscription);
    }

    private void handleCloseMessage(NostrWebSocketSession session, CloseRequest close) {
        if (log.isDebugEnabled()) {
            log.debug("Closing subscription '{}' on CLOSE command for session '{}'", close.getId(), session.getId());
        }

        SessionId sessionId = new SessionId(session.getId());
        SubscriptionId subscriptionId = SubscriptionId.of(close.getId());

        SubscriptionKey key = new SubscriptionKey(sessionId, subscriptionId);
        support.remove(key);
    }
}
