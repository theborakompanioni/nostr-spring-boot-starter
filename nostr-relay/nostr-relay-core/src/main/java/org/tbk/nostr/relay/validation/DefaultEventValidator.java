package org.tbk.nostr.relay.validation;

import org.springframework.validation.Errors;
import org.tbk.nostr.base.*;
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
            case "k" -> validateTagK(tag, errors);
            default -> {
                // empty on purpose
            }
        }
    }

    /**
     * The 'e' tag, used to refer to an event: ["e", <32-bytes lowercase hex of the id of another event>, <recommended relay URL, optional>]
     *
     * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md#tags">NIP-01</a>
     */
    private void validateTagE(TagValue tag, Errors errors) {
        if (tag.getValuesCount() <= 0L) {
            errors.rejectValue("valuesList", "event.e.tag.value.invalid", "Invalid tag 'e'.");
        } else {
            String supposedEventId = tag.getValues(0);
            if (!EventId.isValidEventIdString(supposedEventId)) {
                errors.rejectValue("valuesList", "event.e.tag.value.invalid", "Invalid tag 'e'.");
            }
            if (tag.getValuesCount() >= 2) {
                String supposedRelayUri = tag.getValues(1);
                if (!RelayUri.isValidRelayUriString(supposedRelayUri)) {
                    errors.rejectValue("valuesList", "event.e.tag.value.invalid", "Invalid tag 'e'.");
                }
            }
        }
    }

    /**
     * The 'p' tag, used to refer to another user: ["p", <32-bytes lowercase hex of a pubkey>, <recommended relay URL, optional>]
     *
     * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md#tags">NIP-01</a>
     */
    private void validateTagP(TagValue tag, Errors errors) {
        if (tag.getValuesCount() <= 0L) {
            errors.rejectValue("valuesList", "event.p.tag.value.invalid", "Invalid tag 'p'.");
        } else {
            String supposedPublicKey = tag.getValues(0);
            if (!MorePublicKeys.isValidPublicKeyString(supposedPublicKey)) {
                errors.rejectValue("valuesList", "event.p.tag.value.invalid", "Invalid tag 'p'.");
            }
            if (tag.getValuesCount() >= 2) {
                String supposedRelayUri = tag.getValues(1);
                if (!RelayUri.isValidRelayUriString(supposedRelayUri)) {
                    errors.rejectValue("valuesList", "event.p.tag.value.invalid", "Invalid tag 'p'.");
                }
            }
        }
    }

    /**
     * The 'a' tag, used to refer to an addressable or replaceable event
     * <ul>
     *   <li>for an addressable event: ["a", <kind integer>:<32-bytes lowercase hex of a pubkey>:<d tag value>, <recommended relay URL, optional>]</li>
     *   <li>for a normal replaceable event: ["a", <kind integer>:<32-bytes lowercase hex of a pubkey>:, <recommended relay URL, optional>]</li>
     * </ul>
     *
     * @see <a href="https://github.com/nostr-protocol/nips/blob/master/01.md#tags">NIP-01</a>
     */
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
            if (tag.getValuesCount() >= 2) {
                String supposedRelayUri = tag.getValues(1);
                if (!RelayUri.isValidRelayUriString(supposedRelayUri)) {
                    errors.rejectValue("valuesList", "event.a.tag.value.invalid", "Invalid tag 'a'.");
                }
            }
        }
    }

    private void validateTagK(TagValue tag, Errors errors) {
        if (tag.getValuesCount() <= 0L) {
            errors.rejectValue("valuesList", "event.k.tag.value.invalid", "Invalid tag 'k'.");
        } else {
            String supposedKind = tag.getValues(0);
            if (!Kind.isValidKindString(supposedKind)) {
                errors.rejectValue("valuesList", "event.k.tag.value.invalid", "Invalid tag 'k'.");
            }
        }
    }
}
