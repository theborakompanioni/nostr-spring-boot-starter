package org.tbk.nostr.relay.nip42.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.tbk.nostr.base.Kinds;
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

    @NonNull
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handleAuthMessage(NostrRequestContext context, AuthRequest request) {
        Event authEvent = request.getEvent();

        try {
            checkAuthEvent(context, authEvent);

            Authentication authentication = nip42Support.attemptAuthentication(context, authEvent)
                    .blockOptional()
                    .orElseThrow(() -> new InternalAuthenticationServiceException("error: Provider error."));

            context.setAuthentication(authentication);

            context.add(Response.newBuilder()
                    .setOk(OkResponse.newBuilder()
                            .setEventId(authEvent.getId())
                            .setSuccess(true)
                            .build())
                    .build());

            this.eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authentication, this.getClass()));
        } catch (Exception e) {
            context.clearAuthentication();

            String errorMessage = switch (e) {
                case AuthenticationException ae -> ae.getMessage();
                default -> "error: Unknown error.";
            };

            context.add(Response.newBuilder()
                    .setOk(OkResponse.newBuilder()
                            .setEventId(authEvent.getId())
                            .setSuccess(false)
                            .setMessage(errorMessage)
                            .build())
                    .build());
        }

    }

    private static void checkAuthEvent(NostrRequestContext context, Event authEvent) throws AuthenticationException {
        if (!Nip42.isAuthEvent(authEvent)) {
            throw new AuthenticationServiceException("invalid: Kind must be %d".formatted(Kinds.kindClientAuthentication.getValue()));
        }

        String expectedChallenge = context.getAuthenticationChallenge()
                .orElseThrow(() -> new AuthenticationServiceException("error: No auth challenge associated."));

        String givenChallenge = Nip42.getChallenge(authEvent)
                .orElseThrow(() -> new AuthenticationServiceException("invalid: No auth challenge found."));

        // TODO: check everything according to https://github.com/nostr-protocol/nips/blob/master/42.md#signed-event-verification
        if (!expectedChallenge.equals(givenChallenge)) {
            throw new AuthenticationServiceException("error: Unknown auth challenge.");
        }
    }
}
