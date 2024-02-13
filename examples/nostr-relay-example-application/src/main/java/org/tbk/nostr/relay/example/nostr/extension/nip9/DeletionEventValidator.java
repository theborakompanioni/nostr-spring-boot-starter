package org.tbk.nostr.relay.example.nostr.extension.nip9;

import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.relay.example.nostr.validating.EventValidator;

@AllArgsConstructor
public class DeletionEventValidator implements EventValidator {

    @Override
    public void validateEvent(Event event, Errors errors) {
        if (Nip9.isDeletionEvent(event)) {
            boolean found = false;
            for (int i = 0; i < event.getTagsCount(); i++) {
                TagValue tag = event.getTags(i);
                if (IndexedTag.e.name().equals(tag.getName()) ||
                    IndexedTag.a.name().equals(tag.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                String errorMessage = "Missing '%s' or '%s' tag.".formatted(IndexedTag.e.name(), IndexedTag.a);
                errors.rejectValue("kind", "kind.invalid", errorMessage);
            }
        }
    }
}
