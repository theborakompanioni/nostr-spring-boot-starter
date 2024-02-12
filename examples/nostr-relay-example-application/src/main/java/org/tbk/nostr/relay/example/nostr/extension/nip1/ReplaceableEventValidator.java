package org.tbk.nostr.relay.example.nostr.extension.nip1;

import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.relay.example.nostr.validating.EventValidator;
import org.tbk.nostr.util.MoreTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ReplaceableEventValidator implements EventValidator {

    @Override
    public void validateEvent(Event event, Errors errors) {
        if (Nip1.isParameterizedReplaceableEvent(event)) {
            Nip1Support.IndexedTagName identifier = Nip1Support.IndexedTagName.d;

            TagValue found = null;
            for (int i = 0; i < event.getTagsCount(); i++) {
                TagValue tag = event.getTags(i);
                if (identifier.name().equals(tag.getName())) {
                    if (found != null) {
                        errors.pushNestedPath("tagsList[%d]".formatted(i));
                        String errorMessage =  "Multiple '%s' tags are not allowed.".formatted(identifier.name());
                        errors.rejectValue("valuesList", "valuesList.invalid", errorMessage);
                        errors.popNestedPath();
                        break;
                    }
                    // TODO: Current implementation can handle missing first value of a "d" tag..
                    //  i.e. ["d"] (null is "currently" not supported by our protocol buffers model: ["d", null], ["d", null, "value", ...])
                    //  After all, missing the first "d" tag value is identifiable.
                    //  BUT should it?
                    /*else if (tag.getValuesCount() <= 0) {
                        errors.pushNestedPath("tagsList[%d]".formatted(i));
                        String errorMessage =  "First value of '%s' tag must not be missing.".formatted(identifier.name());
                        errors.rejectValue("valuesList", "valuesList.invalid", errorMessage);
                        errors.popNestedPath();
                        break;
                    }*/

                    found = tag;
                }
            }

            // TODO: Current implementation CAN NOT handle missing the "d" tag-> should it?
            //  After all, missing a "d" tag is also identifiable.
            if (found == null) {
                String errorMessage =  "Missing '%s' tag.".formatted(identifier.name());
                errors.rejectValue("createdAt", "createdAt.invalid", errorMessage);
            }
        }
    }
}
