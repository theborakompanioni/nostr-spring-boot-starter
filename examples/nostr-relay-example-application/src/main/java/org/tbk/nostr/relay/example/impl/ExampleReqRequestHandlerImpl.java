package org.tbk.nostr.relay.example.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;
import org.tbk.nostr.relay.example.nostr.handler.ReqRequestHandler;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ExampleReqRequestHandlerImpl implements ReqRequestHandler {

    @NonNull
    private final EventEntityService eventEntityService;

    @Override
    public void handleReqMessage(NostrWebSocketSession session, ReqRequest req) {
        try {
            handleInternal(session, req);
        } catch (Exception e) {
            session.queueResponse(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage("Error: %s".formatted("Internal error."))
                            .build())
                    .build());
        }
    }

    private void handleInternal(NostrWebSocketSession session, ReqRequest req) {
        List<EventEntity> events = eventEntityService.findAll(req.getFiltersList());

        events.stream()
                .map(it -> Response.newBuilder().setEvent(EventResponse.newBuilder()
                                .setSubscriptionId(req.getId())
                                .setEvent(it.toNostrEvent())
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
