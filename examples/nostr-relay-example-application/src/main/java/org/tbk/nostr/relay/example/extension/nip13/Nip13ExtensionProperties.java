package org.tbk.nostr.relay.example.extension.nip13;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.nip13",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class Nip13ExtensionProperties implements Validator {
    private static final boolean REQUIRE_COMMITMENT_DEFAULT = true;

    private boolean enabled;

    private int minPowDifficulty;

    private Boolean requireCommitment;

    public boolean getRequireCommitment() {
        return requireCommitment != null ? requireCommitment : (REQUIRE_COMMITMENT_DEFAULT && minPowDifficulty > 0);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Nip13ExtensionProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Nip13ExtensionProperties properties = (Nip13ExtensionProperties) target;

        if (properties.minPowDifficulty < 0) {
            String errorMessage = "'minPowDifficulty' must not be negative";
            errors.rejectValue("minPowDifficulty", "minPowDifficulty.invalid", errorMessage);
        }
        if (properties.minPowDifficulty > 256) {
            String errorMessage = "'minPowDifficulty' must not be greater than 256";
            errors.rejectValue("minPowDifficulty", "minPowDifficulty.invalid", errorMessage);
        }
    }
}
