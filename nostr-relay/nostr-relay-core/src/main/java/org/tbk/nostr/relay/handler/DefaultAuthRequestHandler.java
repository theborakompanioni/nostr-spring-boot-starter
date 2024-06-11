package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.AuthRequest;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;

public class DefaultAuthRequestHandler implements AuthRequestHandler {

    @Override
    public void handleAuthMessage(NostrRequestContext context, AuthRequest request) {
        context.add(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("AUTH ist not supported.")
                        .build())
                .build());
    }
}
