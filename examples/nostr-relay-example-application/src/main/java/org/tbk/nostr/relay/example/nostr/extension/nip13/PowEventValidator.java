package org.tbk.nostr.relay.example.nostr.extension.nip13;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;
import org.tbk.nostr.nips.Nip13;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.example.nostr.validation.EventValidator;

@RequiredArgsConstructor
class PowEventValidator implements EventValidator {

    @NonNull
    private final Nip13ExtensionProperties properties;

    @Override
    public void validateEvent(Event event, Errors errors) {
        boolean meetsTargetDifficulty = Nip13.meetsTargetDifficulty(event, properties.getMinPowDifficulty(), properties.getRequireCommitment());
        if (meetsTargetDifficulty) {
            return;
        }

        long difficulty = Nip13.calculateDifficulty(event.getId().toByteArray());

        String errorMessage = "Difficulty %d is less than %d".formatted(difficulty, properties.getMinPowDifficulty());
        errors.rejectValue("id", "id.invalid", errorMessage);
    }
}
