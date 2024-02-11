package org.tbk.nostr.relay.example.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.nostr.handler.ReqRequestHandler;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ExampleReqRequestHandlerImpl implements ReqRequestHandler {

    @NonNull
    private final EventEntityService eventEntityService;

    @Override
    public void handleReqMessage(WebSocketSession session, ReqRequest req) {
        try {
            handleInternal(session, req);
        } catch (Exception e) {
            try {
                session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                        .setNotice(NoticeResponse.newBuilder()
                                .setMessage("Error: %s".formatted("Internal error."))
                                .build())
                        .build())));
            } catch (IOException e2) {
                log.warn("Error while sending error notice message: {}", e2.getMessage());
            }
        }
    }

    private void handleInternal(WebSocketSession session, ReqRequest req) {
        List<EventEntity> events = eventEntityService.findAll(req.getFiltersList());

        for (EventEntity event : events) {
            try {
                session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                        .setEvent(EventResponse.newBuilder()
                                .setSubscriptionId(req.getId())
                                .setEvent(event.toNostrEvent())
                                .build())
                        .build())));
            } catch (IOException e) {
                log.warn("Error while sending event message: {}", e.getMessage());
            }
        }

        try {
            session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                    .setEose(EoseResponse.newBuilder()
                            .setSubscriptionId(req.getId())
                            .build())
                    .build())));
        } catch (IOException e) {
            log.warn("Error while sending eose message: {}", e.getMessage());
        }
    }
}
