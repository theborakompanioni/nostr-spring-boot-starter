package org.tbk.nostr.relay.plugin.allowlist.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.nostr.util.MorePublicKeys;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.plugin.allowlist",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class AllowlistPluginProperties implements Validator {

    private boolean enabled;

    private List<String> allowed;

    public List<String> getAllowed() {
        return allowed == null ? Collections.emptyList() : Collections.unmodifiableList(allowed);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == AllowlistPluginProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        AllowlistPluginProperties properties = (AllowlistPluginProperties) target;

        properties.getAllowed().forEach(it -> {
            if (!MorePublicKeys.isValidPublicKeyString(it)) {
                String errorMessage = "Values must be a valid public key";
                errors.rejectValue("allowed", "allowed.invalid", errorMessage);
            }
        });
    }
}
