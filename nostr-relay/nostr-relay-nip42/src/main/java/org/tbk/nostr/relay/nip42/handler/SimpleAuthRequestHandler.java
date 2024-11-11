package org.tbk.nostr.relay.nip42.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.handler.AuthRequestHandler;
import org.tbk.nostr.relay.nip42.Nip42Support;
import org.tbk.nostr.relay.validation.DefaultEventValidator;
import org.tbk.nostr.relay.validation.EventValidator;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class SimpleAuthRequestHandler implements AuthRequestHandler {

    private static final List<EventValidator> validators = List.of(
            new DefaultEventValidator(),
            new AuthEventValidator()
    );

    @NonNull
    private final Nip42Support nip42Support;

    @Override
    public void handleAuthMessage(NostrRequestContext context, AuthRequest request) {
        Event authEvent = request.getEvent();

        BindException errors = validateAuthEvent(authEvent);
        if (errors.hasErrors()) {
            context.add(Response.newBuilder()
                    .setOk(OkResponse.newBuilder()
                            .setEventId(authEvent.getId())
                            .setSuccess(false)
                            .setMessage("error: %s".formatted(errors.getMessage()))
                            .build())
                    .build());
            return;
        }

        nip42Support.handleAuthEvent(context, authEvent)
                .subscribe(authenticated -> {
                    context.getSession().setAuthenticated(authenticated);

                    if (authenticated) {
                        context.add(Response.newBuilder()
                                .setOk(OkResponse.newBuilder()
                                        .setEventId(authEvent.getId())
                                        .setSuccess(true)
                                        .build())
                                .build());
                    } else {
                        context.add(Response.newBuilder()
                                .setOk(OkResponse.newBuilder()
                                        .setEventId(authEvent.getId())
                                        .setSuccess(false)
                                        .setMessage("error: Not authenticated.")
                                        .build())
                                .build());
                    }
                }, e -> {
                    context.getSession().setAuthenticated(false);

                    context.add(Response.newBuilder()
                            .setOk(OkResponse.newBuilder()
                                    .setEventId(authEvent.getId())
                                    .setSuccess(false)
                                    .setMessage("error: %s".formatted(e.getMessage()))
                                    .build())
                            .build());
                });
    }

    private static BindException validateAuthEvent(Event authEvent) {
        BindException errors = new BindException(authEvent, "event");
        for (EventValidator validator : validators) {
            ValidationUtils.invokeValidator(validator, authEvent, errors);

            if (errors.hasErrors()) {
                break;
            }
        }
        return errors;
    }

    private static class AuthEventValidator implements EventValidator {

        @Override
        public void validateEvent(Event authEvent, Errors errors) {
            if (authEvent.getKind() != 22_242) {
                String errorMessage = "Kind must be be %d".formatted(22_242);
                errors.rejectValue("kind", "kind.invalid", errorMessage);
            }
        }
    }
}
