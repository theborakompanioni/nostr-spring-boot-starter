package org.tbk.nostr.relay.nip42.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tbk.nostr.nips.Nip42;
import org.tbk.nostr.proto.AuthRequest;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.handler.AuthRequestHandler;
import org.tbk.nostr.relay.nip42.Nip42Support;

@RequiredArgsConstructor
public class SimpleAuthRequestHandler implements AuthRequestHandler {

    @NonNull
    private final Nip42Support nip42Support;

    @Override
    public void handleAuthMessage(NostrRequestContext context, AuthRequest request) {
        Event authEvent = request.getEvent();

        if (authEvent.getKind() != Nip42.kind().getValue()) {
            context.add(Response.newBuilder()
                    .setOk(OkResponse.newBuilder()
                            .setEventId(authEvent.getId())
                            .setSuccess(false)
                            .setMessage("invalid: Kind must be %d".formatted(Nip42.kind().getValue()))
                            .build())
                    .build());
            return;
        }

        nip42Support.handleAuthEvent(context, authEvent)
                .subscribe(authentication -> {
                    context.setAuthentication(authentication);

                    context.add(Response.newBuilder()
                            .setOk(OkResponse.newBuilder()
                                    .setEventId(authEvent.getId())
                                    .setSuccess(true)
                                    .build())
                            .build());
                }, e -> {
                    context.clearAuthentication();

                    context.add(Response.newBuilder()
                            .setOk(OkResponse.newBuilder()
                                    .setEventId(authEvent.getId())
                                    .setSuccess(false)
                                    .setMessage("error: %s".formatted(e.getMessage()))
                                    .build())
                            .build());
                });
    }

}
