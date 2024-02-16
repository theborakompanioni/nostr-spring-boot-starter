package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.CountRequest;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrWebSocketSession;

public class DefaultCountRequestHandler implements CountRequestHandler {

    @Override
    public void handleCountMessage(NostrWebSocketSession session, CountRequest count) throws Exception {
        session.sendResponseImmediately(Response.newBuilder()
                .setClosed(ClosedResponse.newBuilder()
                        .setSubscriptionId(count.getId())
                        .setMessage("COUNT ist not supported.")
                        .build())
                .build());
    }
}
