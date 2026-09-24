package org.tbk.nostr.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Nullable;
import java.util.Optional;

@ConfigurationProperties(
        prefix = "org.tbk.nostr",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class NostrProperties implements Validator {

    @Nullable
    private String dontUse;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == NostrProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        NostrProperties properties = (NostrProperties) target;

        if (properties.dontUse != null) {
            String errorMessage = "'dontUse' is a placeholder and must be empty";
            errors.rejectValue("dontUse", "dontUse.invalid", errorMessage);
        }
    }
}
