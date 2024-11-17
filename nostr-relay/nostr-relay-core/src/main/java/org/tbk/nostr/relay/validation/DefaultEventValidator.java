package org.tbk.nostr.relay.validation;

import org.springframework.validation.Errors;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MorePublicKeys;

public class DefaultEventValidator implements EventValidator {
    @Override
    public void validateEvent(Event event, Errors errors) {
        if (event.getId().size() != 32) {
            errors.rejectValue("id", "event.id.invalid", "Invalid id.");
        }
        if (!Kind.isValidKind(event.getKind())) {
            errors.rejectValue("kind", "event.kind.invalid", "Invalid kind.");
        }
        if (event.getCreatedAt() < 0L) {
            errors.rejectValue("createdAt", "event.created_at.invalid", "Invalid created timestamp.");
        }
        if (event.getPubkey().size() != 32) {
            errors.rejectValue("pubkey", "event.pubkey.invalid", "Invalid public key.");
        }

        for (int i = 0; i < event.getTagsCount(); i++) {
            errors.pushNestedPath("tagsList[%d]".formatted(i));
            validateTag(event.getTags(i), errors);
            errors.popNestedPath();
        }

        try {
            MoreEvents.verifySignature(event);
        } catch (IllegalArgumentException e) {
            errors.rejectValue("sig", "event.sig.invalid", e.getMessage());
        }
    }

    private void validateTag(TagValue tag, Errors errors) {
        if (tag.getName().isEmpty()) {
            errors.rejectValue("name", "event.tag.name.invalid", "Invalid tag name.");
        } else if (tag.getName().length() > 256) {
            errors.rejectValue("name", "event.tag.name.invalid", "Invalid tag name.");
        }

        switch (tag.getName()) {
            case "e" -> validateTagE(tag, errors);
            case "p" -> validateTagP(tag, errors);
            case "a" -> validateTagA(tag, errors);
            default -> {
                // empty on purpose
            }
        }
    }

    private void validateTagE(TagValue tag, Errors errors) {
        if (tag.getValuesCount() <= 0L) {
            errors.rejectValue("valuesList", "event.e.tag.value.invalid", "Invalid tag 'e'.");
        } else {
            String supposedEventId = tag.getValues(0);
            if (!EventId.isValidEventIdString(supposedEventId)) {
                errors.rejectValue("valuesList", "event.e.tag.value.invalid", "Invalid tag 'e'.");
            }
        }
    }

    private void validateTagP(TagValue tag, Errors errors) {
        if (tag.getValuesCount() <= 0L) {
            errors.rejectValue("valuesList", "event.p.tag.value.invalid", "Invalid tag 'p'.");
        } else {
            String supposedPublicKey = tag.getValues(0);
            if (!MorePublicKeys.isValidPublicKeyString(supposedPublicKey)) {
                errors.rejectValue("valuesList", "event.p.tag.value.invalid", "Invalid tag 'p'.");
            }
        }
    }

    private void validateTagA(TagValue tag, Errors errors) {
        if (tag.getValuesCount() <= 0L) {
            errors.rejectValue("valuesList", "event.a.tag.value.invalid", "Invalid tag 'a'.");
        } else {
            String supposedEventUri = tag.getValues(0);
            try {
                EventUri eventUri = EventUri.fromString(supposedEventUri);
                if (!MorePublicKeys.isValidPublicKey(eventUri.getPublicKey())) {
                    errors.rejectValue("valuesList", "event.a.tag.pubkey.value.invalid", "Invalid pubkey in tag 'a'.");
                }
            } catch (Exception e) {
                errors.rejectValue("valuesList", "event.a.tag.value.invalid", "Invalid tag 'a'.");
            }
        }
    }
}
