package org.tbk.nostr.relay.nip42.interceptor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.nip42.Nip42Support;

@Slf4j
@RequiredArgsConstructor
public class SimpleAuthenticationInterceptor implements AuthenticationInterceptor {

    @NonNull
    private final Nip42Support nip42Support;

    @Override
    public boolean preHandleUnauthenticated(NostrRequestContext context, Request request) {
        if (Boolean.FALSE.equals(nip42Support.needsAuthentication(context, request).block())) {
            return true;
        }

        onAuthenticationRequired(context, request);
        return false;
    }

    private static void onAuthenticationRequired(NostrRequestContext context, Request request) {
        String errorMessage = "auth-required: authentication required.";
        switch (request.getKindCase()) {
            case CLOSE, AUTH -> {
                // authentication not needed for these events
                throw new IllegalArgumentException("Cannot enforce authentication for events with kind %s".formatted(request.getKindCase()));
            }
            case EVENT -> context.add(Response.newBuilder()
                    .setOk(OkResponse.newBuilder()
                            .setEventId(request.getEvent().getEvent().getId())
                            .setSuccess(false)
                            .setMessage(errorMessage)
                            .build())
                    .build());
            case REQ -> context.add(Response.newBuilder()
                    .setClosed(ClosedResponse.newBuilder()
                            .setSubscriptionId(request.getReq().getId())
                            .setMessage(errorMessage)
                            .build())
                    .build());
            case COUNT -> context.add(Response.newBuilder()
                    .setClosed(ClosedResponse.newBuilder()
                            .setSubscriptionId(request.getCount().getId())
                            .setMessage(errorMessage)
                            .build())
                    .build());
            case KIND_NOT_SET -> context.add(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage(errorMessage)
                            .build())
                    .build());
        }
    }
}
