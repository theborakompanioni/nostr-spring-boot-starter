package org.tbk.nostr.relay.example.nostr.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.example.nostr.NostrSupport;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DefaultReqRequestHandler implements ReqRequestHandler {

    @NonNull
    private final NostrSupport support;

    @Override
    public void handleReqMessage(NostrWebSocketSession session, ReqRequest req) {
        try {
            handleInternal(session, req);
        } catch (Exception e) {
            log.debug("Error while handling REQ message: {}", e.getMessage());

            session.queueResponse(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage("Error: %s".formatted("Internal error."))
                            .build())
                    .build());
        }
    }

    private void handleInternal(NostrWebSocketSession session, ReqRequest req) {
        List<Event> events = support.findAll(req.getFiltersList()).toStream().toList();

        events.stream()
                .map(it -> Response.newBuilder().setEvent(EventResponse.newBuilder()
                                .setSubscriptionId(req.getId())
                                .setEvent(it)
                                .build())
                        .build())
                .forEach(session::queueResponse);

        session.queueResponse(Response.newBuilder()
                .setEose(EoseResponse.newBuilder()
                        .setSubscriptionId(req.getId())
                        .build())
                .build());
    }
}
