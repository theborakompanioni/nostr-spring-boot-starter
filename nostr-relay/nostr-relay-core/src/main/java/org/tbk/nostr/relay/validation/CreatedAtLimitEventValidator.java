package org.tbk.nostr.relay.validation;

import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.tbk.nostr.proto.Event;

import javax.annotation.Nullable;

@AllArgsConstructor
public class CreatedAtLimitEventValidator implements EventValidator {

    @Nullable
    private Long createdAtLowerLimit;

    @Nullable
    private Long createdAtUpperLimit;

    @Override
    public void validateEvent(Event event, Errors errors) {
        if (createdAtLowerLimit != null) {
            if (event.getCreatedAt() < createdAtLowerLimit) {
                errors.rejectValue("createdAt", "createdAt.invalid",
                        new Object[]{String.valueOf(event.getCreatedAt()), String.valueOf(createdAtLowerLimit)},
                        "''created_at'' timestamp {0} is less than lower limit {1}.");
            }
        }

        if (createdAtUpperLimit != null) {
            if (event.getCreatedAt() > createdAtUpperLimit) {
                errors.rejectValue("createdAt", "createdAt.invalid",
                        new Object[]{String.valueOf(event.getCreatedAt()), String.valueOf(createdAtUpperLimit)},
                        "''created_at'' timestamp {0} is greater than upper limit {1}.");
            }
        }
    }
}
