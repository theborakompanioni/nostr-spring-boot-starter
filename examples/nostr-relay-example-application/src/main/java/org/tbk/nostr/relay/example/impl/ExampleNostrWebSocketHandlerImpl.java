package org.tbk.nostr.relay.example.impl;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties.RelayOptionsProperties;
import org.tbk.nostr.relay.example.nostr.NostrRequestHandlerSupport;
import org.tbk.nostr.relay.example.nostr.handler.*;

import static java.util.Objects.requireNonNull;

public class ExampleNostrWebSocketHandlerImpl extends NostrRequestHandlerSupport {
    private final RelayOptionsProperties relayOptions;

    public ExampleNostrWebSocketHandlerImpl(RelayOptionsProperties relayOptions,
                                            ReqRequestHandler reqRequestHandler,
                                            EventRequestHandler eventRequestHandler,
                                            CloseRequestHandler closeRequestHandler,
                                            CountRequestHandler countRequestHandler,
                                            UnknownRequestHandler unknownRequestHandler) {
        super(reqRequestHandler, eventRequestHandler, closeRequestHandler, countRequestHandler, unknownRequestHandler);
        this.relayOptions = requireNonNull(relayOptions);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (relayOptions.getGreeting().isPresent()) {
            session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage(relayOptions.getGreeting().get())
                            .build())
                    .build())));
        }
    }
}
