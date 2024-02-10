package org.tbk.nostr.relay.example.nostr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties;

import javax.annotation.Nullable;
import java.util.Optional;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.relay",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class NostrRelayProperties implements Validator {
    private static final String DEFAULT_WEBSOCKET_PATH = "/";

    private static final int MAX_LIMIT_PER_FILTER_DEFAULT = 1_000;

    private static final int MAX_FILTER_COUNT_DEFAULT = 21;

    @Nullable
    private String websocketPath;

    @Nullable
    private Integer maxLimitPerFilter;

    @Nullable
    private Integer maxFilterCount;

    public String getWebsocketPath() {
        return Optional.ofNullable(websocketPath)
                .filter(it -> !it.isBlank())
                .orElse(DEFAULT_WEBSOCKET_PATH);
    }

    public int getMaxLimitPerFilter() {
        return Optional.ofNullable(maxLimitPerFilter).orElse(MAX_LIMIT_PER_FILTER_DEFAULT);
    }

    public int getMaxFilterCount() {
        return Optional.ofNullable(maxFilterCount).orElse(MAX_FILTER_COUNT_DEFAULT);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == NostrRelayProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        NostrRelayProperties properties = (NostrRelayProperties) target;

        if (properties.getMaxLimitPerFilter() <= 0) {
            String errorMessage = "'maxLimitPerFilter' must be greater than zero";
            errors.rejectValue("maxLimitPerFilter", "maxLimitPerFilter.invalid", errorMessage);
        }
        if (properties.getMaxFilterCount() <= 0) {
            String errorMessage = "'maxFilterCount' must be greater than zero";
            errors.rejectValue("maxFilterCount", "maxFilterCount.invalid", errorMessage);
        }
    }
}
