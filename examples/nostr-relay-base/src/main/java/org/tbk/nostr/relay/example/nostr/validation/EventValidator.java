package org.tbk.nostr.relay.example.nostr.validation;

import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.nostr.proto.Event;

public interface EventValidator extends Validator {
    default boolean supports(Class<?> clazz) {
        return clazz == Event.class;
    }

    default void validate(Object target, Errors errors) {
        Assert.isTrue(supports(target.getClass()), "Given class not supported.");
        this.validateEvent((Event) target, errors);
    }

    void validateEvent(Event target, Errors errors);
}
