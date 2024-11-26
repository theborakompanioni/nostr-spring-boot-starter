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
        if (Boolean.FALSE.equals(nip42Support.requiresAuthentication(context, request).block())) {
            return true;
        }
        context.add(createAuthenticationRequiredResponse(request));

        String challenge = context.getAuthenticationChallenge()
                .orElseGet(() -> nip42Support.createNewChallenge(context.getSession()));
        context.setAuthenticationChallenge(challenge);

        context.add(Response.newBuilder()
                .setAuth(AuthResponse.newBuilder()
                        .setChallenge(challenge)
                        .build())
                .build());

        return false;
    }

    private static Response createAuthenticationRequiredResponse(Request request) {
        String errorMessage = "auth-required: authentication required.";
        return switch (request.getKindCase()) {
            case CLOSE, AUTH -> {
                // authentication not needed for these events
                throw new IllegalArgumentException("Cannot enforce authentication for events with kind %s".formatted(request.getKindCase()));
            }
            case EVENT -> Response.newBuilder()
                    .setOk(OkResponse.newBuilder()
                            .setEventId(request.getEvent().getEvent().getId())
                            .setSuccess(false)
                            .setMessage(errorMessage)
                            .build())
                    .build();
            case REQ -> Response.newBuilder()
                    .setClosed(ClosedResponse.newBuilder()
                            .setSubscriptionId(request.getReq().getId())
                            .setMessage(errorMessage)
                            .build())
                    .build();
            case COUNT -> Response.newBuilder()
                    .setClosed(ClosedResponse.newBuilder()
                            .setSubscriptionId(request.getCount().getId())
                            .setMessage(errorMessage)
                            .build())
                    .build();
            case KIND_NOT_SET -> Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage(errorMessage)
                            .build())
                    .build();
        };
    }
}
