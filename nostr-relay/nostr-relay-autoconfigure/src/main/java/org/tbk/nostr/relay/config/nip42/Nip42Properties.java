package org.tbk.nostr.relay.config.nip42;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.Duration;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.nip42",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class Nip42Properties implements Validator {
    private static final Duration DEFAULT_MAX_CHALLENGE_TIMESTAMP_OFFSET_INTERVAL = Duration.ofMinutes(10);

    private boolean enabled;

    private Duration maxChallengeTimestampOffsetInterval;

    public Duration getMaxChallengeTimestampOffsetInterval() {
        return maxChallengeTimestampOffsetInterval != null ? maxChallengeTimestampOffsetInterval : DEFAULT_MAX_CHALLENGE_TIMESTAMP_OFFSET_INTERVAL;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Nip42Properties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Nip42Properties properties = (Nip42Properties) target;

        if (properties.getMaxChallengeTimestampOffsetInterval().isNegative()) {
            String errorMessage = "'maxChallengeTimestampOffsetInterval' must be not be negative";
            errors.rejectValue("maxChallengeTimestampOffsetInterval", "maxChallengeTimestampOffsetInterval.invalid", errorMessage);
        }
    }
}
