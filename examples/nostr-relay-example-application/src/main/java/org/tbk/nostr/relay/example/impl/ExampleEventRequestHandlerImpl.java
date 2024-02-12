package org.tbk.nostr.relay.example.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.UncategorizedDataAccessException;
import org.sqlite.SQLiteException;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;
import org.tbk.nostr.relay.example.nostr.handler.EventRequestHandler;

@Slf4j
@RequiredArgsConstructor
public class ExampleEventRequestHandlerImpl implements EventRequestHandler {

    @NonNull
    private final EventEntityService eventEntityService;

    @Override
    public void handleEventMessage(NostrWebSocketSession session, EventRequest event) throws Exception {
        OkResponse.Builder okBuilder = OkResponse.newBuilder()
                .setEventId(event.getEvent().getId())
                .setSuccess(false);

        try {
            EventEntity eventEntity = eventEntityService.createEvent(event.getEvent());
            okBuilder.setSuccess(eventEntity != null);
        } catch (Exception e) {
            okBuilder.mergeFrom(handleEventMessageException(e, okBuilder).buildPartial());
        }

        session.queueResponse(Response.newBuilder()
                .setOk(okBuilder.build())
                .build());
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
