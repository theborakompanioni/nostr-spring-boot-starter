package org.tbk.nostr.relay.example.impl;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.sqlite.SQLiteException;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.domain.event.EventEntitySpecifications;
import org.tbk.nostr.relay.example.nostr.handler.EventRequestHandler;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MorePublicKeys;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class ExampleEventRequestHandlerImpl implements EventRequestHandler {

    @NonNull
    private final EventEntityService eventEntityService;

    @Override
    public void handleEventMessage(WebSocketSession session, EventRequest event) throws Exception {
        OkResponse.Builder okBuilder = OkResponse.newBuilder()
                .setEventId(event.getEvent().getId())
                .setSuccess(false);

        try {
            EventEntity eventEntity = eventEntityService.createEvent(event.getEvent());
            okBuilder.setSuccess(eventEntity != null);
        } catch (Exception e) {
            okBuilder.mergeFrom(handleEventMessageException(e, okBuilder).buildPartial());
        }

        session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                .setOk(okBuilder.build())
                .build())));
    }

    private static OkResponse.Builder handleEventMessageException(Exception e, OkResponse.Builder okBuilder) {
        if (e instanceof UncategorizedDataAccessException udae) {
            okBuilder.setMessage("Error: %s".formatted("Undefined storage error."));

            Throwable mostSpecificCause = udae.getMostSpecificCause();
            if (mostSpecificCause instanceof SQLiteException sqliteException) {
                okBuilder.setMessage("Error: %s".formatted("Storage error (%d).".formatted(sqliteException.getResultCode().code)));
                switch (sqliteException.getResultCode()) {
                    case SQLITE_CONSTRAINT_UNIQUE, SQLITE_CONSTRAINT_PRIMARYKEY ->
                            okBuilder.setMessage("Error: %s".formatted("Duplicate event."));
                    case SQLITE_CONSTRAINT_CHECK -> okBuilder.setMessage("Error: %s".formatted("Check failed."));
                }
            }
        } else if (e instanceof DataIntegrityViolationException) {
            okBuilder.setMessage("Error: %s".formatted("Duplicate event."));
        } else {
            okBuilder.setMessage("Error: %s".formatted("Unknown reason."));
        }

        return okBuilder;
    }
}
