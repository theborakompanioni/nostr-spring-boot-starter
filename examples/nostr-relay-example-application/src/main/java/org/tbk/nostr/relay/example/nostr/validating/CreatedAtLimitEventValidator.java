package org.tbk.nostr.relay.example.nostr.validating;

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
                String errorMessage = "'created_at' timestamp %d is less than lower limit %d.".formatted(event.getCreatedAt(), createdAtLowerLimit);
                errors.rejectValue("createdAt", "createdAt.invalid", errorMessage);
            }
        }

        if (createdAtUpperLimit != null) {
            if (event.getCreatedAt() > createdAtUpperLimit) {
                String errorMessage = "'created_at' timestamp %d is greater than upper limit %d.".formatted(event.getCreatedAt(), createdAtUpperLimit);
                errors.rejectValue("createdAt", "createdAt.invalid", errorMessage);
            }
        }
    }
}
