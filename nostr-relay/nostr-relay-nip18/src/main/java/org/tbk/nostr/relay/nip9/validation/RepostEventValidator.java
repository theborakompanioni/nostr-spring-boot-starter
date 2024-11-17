package org.tbk.nostr.relay.nip9.validation;

import com.google.protobuf.ByteString;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.nips.Nip18;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.relay.validation.EventValidator;
import org.tbk.nostr.util.MoreTags;

import java.util.Optional;

@RequiredArgsConstructor
public class RepostEventValidator implements EventValidator {

    @NonNull
    private final EventValidator repostedEventValidator;

    @Override
    public void validateEvent(Event event, Errors errors) {
        if (Nip18.isRepostEvent(event)) {
            try {
                Event repostedEvent = JsonReader.fromJson(event.getContent(), Event.newBuilder());

                if (event.getKind() == Nip18.kindRepost().getValue()) {
                    if (repostedEvent.getKind() != 1) {
                        errors.rejectValue("kind", "nip18.refs.invalid", "Reposted event must be a short text note.");
                        return;
                    }
                } else if (event.getKind() == Nip18.kindGenericRepost().getValue()) {
                    if (repostedEvent.getKind() == 1) {
                        errors.rejectValue("kind", "nip18.refs.invalid", "Reposted event must not be a short text note.");
                        return;
                    }
                }

                Optional<TagValue> eTagRepostedEventRefOrEmpty = MoreTags.findByName(event, IndexedTag.e).stream()
                        .filter(it -> repostedEvent.getId().equals(ByteString.fromHex(it.getValues(0))))
                        .findFirst();

                if (eTagRepostedEventRefOrEmpty.isEmpty()) {
                    errors.rejectValue("kind", "nip18.tags.invalid",
                            new Object[]{IndexedTag.e.name()},
                            "Invalid ''{0}'' tag. Must include reposted event id.");
                    return;
                } else {
                    TagValue eTagRepostedEventRef = eTagRepostedEventRefOrEmpty.get();
                    if (eTagRepostedEventRef.getValuesCount() < 2) {
                        errors.rejectValue("kind", "nip18.tags.invalid",
                                new Object[]{IndexedTag.e.name()},
                                "Invalid ''{0}'' tag. Missing relay URL.");
                        return;
                    }
                }

                BindException innerEventErrors = new BindException(repostedEvent, "event");
                ValidationUtils.invokeValidator(repostedEventValidator, repostedEvent, innerEventErrors);

                if (innerEventErrors.hasErrors()) {
                    innerEventErrors.getAllErrors().stream().findFirst().ifPresent(it -> {
                        errors.rejectValue("content", "nip18.refs.invalid", it.getArguments(), it.getDefaultMessage());
                    });
                }
            } catch (Exception e) {
                errors.rejectValue("content", "nip18.refs.invalid", "error: Reposted event cannot be read.");
            }
        }
    }
}
