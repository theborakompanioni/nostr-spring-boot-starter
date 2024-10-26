package org.tbk.nostr.relay.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.NostrSupport;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DefaultReqRequestHandler implements ReqRequestHandler {

    @NonNull
    private final NostrSupport support;

    @Override
    public void handleReqMessage(NostrRequestContext context, ReqRequest req) {
        try {
            handleInternal(context, req);
        } catch (Exception e) {
            log.debug("Error while handling REQ message: {}", e.getMessage());

            context.add(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage("error: %s".formatted("Internal error."))
                            .build())
                    .build());
        }
    }

    private void handleInternal(NostrRequestContext context, ReqRequest req) {
        List<Event> events = support.findAll(req.getFiltersList()).toStream().toList();

        events.stream()
                .map(it -> Response.newBuilder().setEvent(EventResponse.newBuilder()
                                .setSubscriptionId(req.getId())
                                .setEvent(it)
                                .build())
                        .build())
                .forEach(context::add);

        context.add(Response.newBuilder()
                .setEose(EoseResponse.newBuilder()
                        .setSubscriptionId(req.getId())
                        .build())
                .build());
    }
}
