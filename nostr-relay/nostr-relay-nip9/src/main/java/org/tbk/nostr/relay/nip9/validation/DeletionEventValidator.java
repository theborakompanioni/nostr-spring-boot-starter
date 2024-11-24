package org.tbk.nostr.relay.nip9.validation;

import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.relay.validation.EventValidator;
import org.tbk.nostr.util.MoreTags;

import java.util.HexFormat;
import java.util.List;

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
                errors.rejectValue("kind", "nip9.tags.invalid",
                        new Object[]{IndexedTag.e.name(), IndexedTag.a},
                        "Missing ''{0}'' or ''{1}'' tag.");
                return;
            }

            List<EventUri> referencedEvents = MoreTags.findByName(event, IndexedTag.a).stream()
                    .map(it -> it.getValues(0))
                    .map(EventUri::parse)
                    .toList();

            String authorPublicKeyHex = HexFormat.of().formatHex(event.getPubkey().toByteArray());
            for (EventUri uri : referencedEvents) {
                if (!authorPublicKeyHex.equals(uri.getPublicKeyHex())) {
                    String errorMessage = "Referencing events not signed by author is not permitted.";
                    errors.rejectValue("kind", "nip9.refs.invalid", errorMessage);
                    break;
                }
            }
        }
    }
}
