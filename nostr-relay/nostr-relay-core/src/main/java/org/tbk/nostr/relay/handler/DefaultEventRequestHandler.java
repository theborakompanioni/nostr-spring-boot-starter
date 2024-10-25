package org.tbk.nostr.relay.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.NostrSupport;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class DefaultEventRequestHandler implements EventRequestHandler {

    @NonNull
    private final NostrSupport support;

    @Override
    public void handleEventMessage(NostrRequestContext context, EventRequest event) throws Exception {
        OkResponse.Builder okBuilder = OkResponse.newBuilder()
                .setEventId(event.getEvent().getId())
                .setSuccess(false);

        try {
            OkResponse ok = support.createEvent(event.getEvent())
                    .blockOptional(Duration.ofSeconds(60))
                    .orElseThrow(() -> new IllegalStateException("Timeout during invocation of 'createEvent'."));
            
            okBuilder.mergeFrom(ok);

            if (ok.getSuccess()) {
                context.setHandledEvent(event.getEvent());
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Unhandled error while creating event: {}", e.getMessage());
            }
            okBuilder.setMessage("error: Unknown reason.");
        }

        context.add(Response.newBuilder()
                .setOk(okBuilder.build())
                .build());
    }
}
