package org.tbk.nostr.relay.validation;

import org.springframework.validation.Errors;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreKinds;
import org.tbk.nostr.util.MorePublicKeys;

import java.util.HexFormat;

public class DefaultEventValidator implements EventValidator {
    @Override
    public void validateEvent(Event event, Errors errors) {
        if (event.getId().size() != 32) {
            errors.rejectValue("id", "id.invalid", "Invalid id.");
        }
        if (!MoreKinds.isValidKind(event.getKind())) {
            errors.rejectValue("kind", "kind.invalid", "Invalid kind.");
        }
        if (event.getCreatedAt() < 0L) {
            errors.rejectValue("createdAt", "createdAt.invalid", "Invalid created timestamp.");
        }
        if (event.getPubkey().size() != 32) {
            errors.rejectValue("pubkey", "pubkey.invalid", "Invalid public key.");
        }

        for (int i = 0; i < event.getTagsCount(); i++) {
            errors.pushNestedPath("tagsList[%d]".formatted(i));
            validateTag(event.getTags(i), errors);
            errors.popNestedPath();
        }

        try {
            MoreEvents.verifySignature(event);
        } catch (IllegalArgumentException e) {
            errors.rejectValue("sig", "sig.invalid", e.getMessage());
        }
    }

    private void validateTag(TagValue tag, Errors errors) {
        if (tag.getName().isEmpty()) {
            errors.rejectValue("name", "name.invalid", "Invalid tag name.");
        } else if (tag.getName().length() > 256) {
            errors.rejectValue("name", "name.invalid", "Invalid tag name.");
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
            errors.rejectValue("valuesList", "valuesList.invalid", "Invalid tag 'e'.");
        } else {
            String supposedEventId = tag.getValues(0);
            if (!EventId.isValidEventIdString(supposedEventId)) {
                errors.rejectValue("valuesList", "valuesList.invalid", "Invalid tag 'e'.");
            }
        }
    }

    private void validateTagP(TagValue tag, Errors errors) {
        if (tag.getValuesCount() <= 0L) {
            errors.rejectValue("valuesList", "valuesList.invalid", "Invalid tag 'p'.");
        } else {
            String supposedPublicKey = tag.getValues(0);
            if (!isValidPublicKey(supposedPublicKey)) {
                errors.rejectValue("valuesList", "valuesList.invalid", "Invalid tag 'p'.");
            }
        }
    }

    private void validateTagA(TagValue tag, Errors errors) {
        if (tag.getValuesCount() <= 0L) {
            errors.rejectValue("valuesList", "valuesList.invalid", "Invalid tag 'a'.");
        } else {
            String supposedEventUri = tag.getValues(0);
            try {
                EventUri eventUri = EventUri.fromString(supposedEventUri);
                if (!isValidPublicKey(eventUri.getPublicKey())) {
                    errors.rejectValue("valuesList", "valuesList.invalid", "Invalid pubkey in tag 'a'.");
                }
            } catch (Exception e) {
                errors.rejectValue("valuesList", "valuesList.invalid", "Invalid tag 'a'.");
            }
        }
    }

    private boolean isValidPublicKey(String value) {
        if (value.length() != 64) {
            return false;
        }
        try {
            return isValidPublicKey(HexFormat.of().parseHex(value));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidPublicKey(byte[] raw) {
        if (raw.length != 32) {
            return false;
        }
        try {
            return MorePublicKeys.isValidPublicKey(raw);
        } catch (Exception e) {
            return false;
        }
    }
}
