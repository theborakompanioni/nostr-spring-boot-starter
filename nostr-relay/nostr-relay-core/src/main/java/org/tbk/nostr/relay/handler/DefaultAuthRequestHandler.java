package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.AuthRequest;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;

public class DefaultAuthRequestHandler implements AuthRequestHandler {

    @Override
    public void handleAuthMessage(NostrRequestContext context, AuthRequest request) {
        context.add(Response.newBuilder()
                .setOk(OkResponse.newBuilder()
                        .setEventId(request.getEvent().getId())
                        .setSuccess(false)
                        .setMessage("error: AUTH is not supported.")
                        .build())
                .build());
    }
}
