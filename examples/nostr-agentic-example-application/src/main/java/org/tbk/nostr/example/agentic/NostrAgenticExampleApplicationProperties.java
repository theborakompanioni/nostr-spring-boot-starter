package org.tbk.nostr.example.agentic;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.net.URI;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.agentic.client",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class NostrAgenticExampleApplicationProperties implements Validator {

    private String relayUri;

    public URI getRelayUri() {
        return URI.create(relayUri);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == NostrAgenticExampleApplicationProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        NostrAgenticExampleApplicationProperties properties = (NostrAgenticExampleApplicationProperties) target;

        String relayUri = properties.relayUri;
        if (Strings.isNullOrEmpty(relayUri)) {
            String errorMessage = "'relayUri' entry must not be empty";
            errors.rejectValue("relayUri", "relayUri.invalid", errorMessage);
        } else if (!relayUri.startsWith("ws://") && !relayUri.startsWith("wss://")) {
            String errorMessage = "'relayUri' must start with 'ws://' or 'wss://'";
            errors.rejectValue("relayUri", "relayUri.invalid", errorMessage);
        } else {
            try {
                @SuppressWarnings("unused")
                var unusedOnPurpose = URI.create(relayUri);
            } catch (IllegalArgumentException e) {
                String errorMessage = "'relayUri' must be a valid URI";
                errors.rejectValue("relayUri", "relayUri.invalid", errorMessage);
            }
        }
    }
}
