package org.tbk.nostr.relay.nip13.validation;

import org.springframework.validation.Errors;
import org.tbk.nostr.nips.Nip13;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.validation.EventValidator;

public class ProofOfWorkEventValidator implements EventValidator {

    private final int minPowDifficulty;

    private final boolean requireCommitment;

    public ProofOfWorkEventValidator(int minPowDifficulty, boolean requireCommitment) {
        if (minPowDifficulty < 0 || minPowDifficulty > 256) {
            throw new IllegalArgumentException("'minPowDifficulty' must be between 0 and 256.");
        }

        this.minPowDifficulty = minPowDifficulty;
        this.requireCommitment = requireCommitment;
    }

    @Override
    public void validateEvent(Event event, Errors errors) {
        if (minPowDifficulty == 0) {
            return;
        }

        boolean meetsTargetDifficulty = Nip13.meetsTargetDifficulty(event, minPowDifficulty, requireCommitment);
        if (!meetsTargetDifficulty) {
            long difficulty = Nip13.calculateDifficulty(event.getId().toByteArray());

            errors.rejectValue("id", "id.invalid",
                    new Object[] { difficulty, minPowDifficulty },
                    "Difficulty {0, number, integer} is less than {1, number, integer}.");
        }
    }
}
