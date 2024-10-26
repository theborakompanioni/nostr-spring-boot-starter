package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;

public class DefaultUnknownRequestHandler implements UnknownRequestHandler {

    @Override
    public void handleUnknownMessage(NostrRequestContext context, Request request) {
        context.add(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("error: %s".formatted("Cannot handle message of unknown type."))
                        .build())
                .build());
    }
}
