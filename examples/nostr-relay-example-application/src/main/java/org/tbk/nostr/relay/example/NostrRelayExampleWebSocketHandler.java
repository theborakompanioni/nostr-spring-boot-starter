package org.tbk.nostr.relay.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.sqlite.SQLiteException;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.nostr.AbstractNostrWebSocketHandler;

@RequiredArgsConstructor
class NostrRelayExampleWebSocketHandler extends AbstractNostrWebSocketHandler {

    @NonNull
    private final EventEntityService eventEntityService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("GM")
                        .build())
                .build())));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {

    }

    @Override
    public void handleEventMessage(WebSocketSession session, EventRequest event) throws Exception {
        OkResponse.Builder okBuilder = OkResponse.newBuilder()
                .setEventId(event.getEvent().getId())
                .setSuccess(false);

        try {
            EventEntity eventEntity = eventEntityService.createEvent(event.getEvent());
            okBuilder.setSuccess(eventEntity != null);
        } catch (JpaSystemException e) {
            okBuilder.setMessage("Error: %s".formatted("Undefined storage error"));

            Throwable mostSpecificCause = e.getMostSpecificCause();
            if (mostSpecificCause instanceof SQLiteException sqliteException) {
                okBuilder.setMessage("Error: %s".formatted("Storage error (%d)".formatted(sqliteException.getResultCode().code)));
                switch (sqliteException.getResultCode()) {
                    case SQLITE_CONSTRAINT_UNIQUE,
                            SQLITE_CONSTRAINT_PRIMARYKEY -> {
                        okBuilder.setMessage("Error: %s".formatted("Duplicate event."));
                    }
                    case SQLITE_CONSTRAINT_CHECK -> {
                        okBuilder.setMessage("Error: %s".formatted("Check failed."));
                    }
                }
            }
        } catch (Exception e) {
            okBuilder.setMessage("Error: %s".formatted("Unknown reason"));
        }

        session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                .setOk(okBuilder.build())
                .build())));
    }

    @Override
    public void handleReqMessage(WebSocketSession session, ReqRequest req) {

    }

    @Override
    public void handleCloseMessage(WebSocketSession session, CloseRequest close) {

    }

    @Override
    public void handleCountMessage(WebSocketSession session, CountRequest count) {

    }

    @Override
    public void handleUnknownMessage(WebSocketSession session, Request request) {

    }
}
