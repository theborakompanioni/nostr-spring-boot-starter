package org.tbk.nostr.relay.example.impl;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties;
import org.tbk.nostr.relay.example.nostr.NostrRequestHandlerSupport;
import org.tbk.nostr.relay.example.nostr.handler.*;

import static java.util.Objects.requireNonNull;

public class ExampleNostrWebSocketHandlerImpl extends NostrRequestHandlerSupport {

    private final NostrRelayExampleApplicationProperties properties;

    public ExampleNostrWebSocketHandlerImpl(NostrRelayExampleApplicationProperties properties,
                                            ReqRequestHandler reqRequestHandler,
                                            EventRequestHandler eventRequestHandler,
                                            CloseRequestHandler closeRequestHandler,
                                            CountRequestHandler countRequestHandler,
                                            UnknownRequestHandler unknownRequestHandler) {
        super(reqRequestHandler, eventRequestHandler, closeRequestHandler, countRequestHandler, unknownRequestHandler);
        this.properties = requireNonNull(properties);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (properties.getGreeting().isPresent()) {
            session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage(properties.getGreeting().get())
                            .build())
                    .build())));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {

    }
}