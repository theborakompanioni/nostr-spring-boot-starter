package org.tbk.nostr.relay.nip42.validation;

import org.springframework.validation.Errors;
import org.tbk.nostr.nips.Nip42;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.validation.EventValidator;

public class AuthEventValidator implements EventValidator {

    @Override
    public void validateEvent(Event event, Errors errors) {
        if (Nip42.isAuthEvent(event)) {
            Nip42.getChallenge(event).ifPresentOrElse(AuthEventValidator::noop, () -> {
                errors.rejectValue("kind", "event.tags.missing.invalid",
                        new Object[]{"challenge"},
                        "Missing ''{0}'' tag.");
            });

            Nip42.getRelay(event).ifPresentOrElse(AuthEventValidator::noop, () -> {
                errors.rejectValue("kind", "event.tags.missing.invalid",
                        new Object[]{"relay"},
                        "Missing ''{0}'' tag.");
            });
        }
    }

    private static <T> void noop(T s) {
    }
}
