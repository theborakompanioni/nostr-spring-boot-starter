package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.CountRequest;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;

public class DefaultCountRequestHandler implements CountRequestHandler {

    @Override
    public void handleCountMessage(NostrRequestContext context, CountRequest count) throws Exception {
        context.add(Response.newBuilder()
                .setClosed(ClosedResponse.newBuilder()
                        .setSubscriptionId(count.getId())
                        .setMessage("COUNT ist not supported.")
                        .build())
                .build());
    }
}
