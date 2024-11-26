package org.tbk.nostr.relay.nip70.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.nips.Nip70;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;
import org.tbk.nostr.util.MorePublicKeys;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ProtectedEventInterceptor implements RequestHandlerInterceptor {

    @Override
    public boolean preHandle(NostrRequestContext context, Request request) {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            return handleEvent(context, request.getEvent().getEvent());
        }
        return true;
    }

    private boolean handleEvent(NostrRequestContext context, Event event) {
        if (Nip70.isProtectedEvent(event)) {
            return handleProtectedEvent(context, event);
        }
        return true;
    }

    private boolean handleProtectedEvent(NostrRequestContext context, Event event) {
        Optional<Principal> authorPrincipal = context.getAuthentication()
                .filter(it -> it.getName().equals(MorePublicKeys.fromEvent(event).value.toHex()));

        if (authorPrincipal.isEmpty()) {
            context.add(Response.newBuilder()
                    .setOk(OkResponse.newBuilder()
                            .setEventId(event.getId())
                            .setSuccess(false)
                            .setMessage("auth-required: This event may only be published by its author.")
                            .build())
                    .build());
            return false;
        }

        return true;
    }
}
