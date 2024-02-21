package org.tbk.nostr.relay.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.CloseRequest;
import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;

@Slf4j
@RequiredArgsConstructor
public class DefaultCloseRequestHandler implements CloseRequestHandler {

    @Override
    public void handleCloseMessage(NostrRequestContext context, CloseRequest close) {
        context.add(Response.newBuilder()
                .setClosed(ClosedResponse.newBuilder()
                        .setSubscriptionId(close.getId())
                        .build())
                .build());
    }
}
