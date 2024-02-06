package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;

public class VerifyingNostrWebSocketHandlerDecorator extends NostrWebSocketHandlerDecorator {

    public VerifyingNostrWebSocketHandlerDecorator(NostrWebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void handleJsonParseException(WebSocketSession session, TextMessage message, Exception e) throws Exception {
        getDelegate().handleJsonParseException(session, message, e);
    }

    @Override
    public void handleEventMessage(WebSocketSession session, EventRequest event) throws Exception {
        if (MoreEvents.isValid(event.getEvent())) {
            getDelegate().handleEventMessage(session, event);
        } else {
            session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage("Invalid event data")
                            .build())
                    .build())));
        }
    }

    @Override
    public void handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception {
        getDelegate().handleReqMessage(session, req);
    }

    @Override
    public void handleCloseMessage(WebSocketSession session, CloseRequest close) throws Exception {
        getDelegate().handleCloseMessage(session, close);
    }

    @Override
    public void handleCountMessage(WebSocketSession session, CountRequest count) throws Exception {
        getDelegate().handleCountMessage(session, count);
    }

    @Override
    public void handleUnknownMessage(WebSocketSession session, Request request) throws Exception {
        getDelegate().handleUnknownMessage(session, request);
    }
}
