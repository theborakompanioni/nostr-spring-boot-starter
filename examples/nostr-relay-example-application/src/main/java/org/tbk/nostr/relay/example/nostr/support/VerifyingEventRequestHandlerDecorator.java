package org.tbk.nostr.relay.example.nostr.support;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.nostr.handler.EventRequestHandler;
import org.tbk.nostr.util.MoreEvents;

@RequiredArgsConstructor
public class VerifyingEventRequestHandlerDecorator implements EventRequestHandler {

    @NonNull
    private final EventRequestHandler delegate;

    @Override
    public void handleEventMessage(WebSocketSession session, EventRequest event) throws Exception {
        if (MoreEvents.isValid(event.getEvent())) {
            delegate.handleEventMessage(session, event);
        } else {
            session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage("Error: %s".formatted("Invalid event."))
                            .build())
                    .build())));
        }
    }
}
