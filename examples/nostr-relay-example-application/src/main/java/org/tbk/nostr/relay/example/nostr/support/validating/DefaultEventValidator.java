package org.tbk.nostr.relay.example.nostr.support.validating;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.util.MoreEvents;

@Component
public class DefaultEventValidator implements EventValidator {
    @Override
    public void validate(Event event, Errors errors) {
        if (event.getId().size() != 32) {
            errors.rejectValue("id", "id.invalid", "Invalid id.");
        }
        if (event.getKind() < 0 || event.getKind() > 65_535) {
            errors.rejectValue("kind", "kind.invalid", "Invalid kind.");
        }
        if (event.getCreatedAt() < 0L) {
            errors.rejectValue("createdAt", "createdAt.invalid", "Invalid created timestamp.");
        }
        if (event.getPubkey().size() != 32) {
            errors.rejectValue("pubkey", "pubkey.invalid", "Invalid public key.");
        }

        try {
            MoreEvents.verifySignature(event);
        } catch (IllegalArgumentException e) {
            errors.rejectValue("sig", "sig.invalid", e.getMessage());
        }
    }
}
