package org.tbk.nostr.relay.plugin.allowlist.config;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tbk.nostr.nip19.Nip19;
import org.tbk.nostr.nip19.Nip19Type;
import org.tbk.nostr.nip19.Npub;
import org.tbk.nostr.util.MorePublicKeys;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.plugin.allowlist",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class AllowlistPluginProperties implements Validator {

    private boolean enabled;

    private List<String> allowed;

    public List<XonlyPublicKey> getAllowed() {
        return allowed == null ? Collections.emptyList() : allowed.stream()
                .map(AllowlistPluginProperties::tryParsePublicKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(it -> it.getPublicKey().isValid())
                .toList();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == AllowlistPluginProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        AllowlistPluginProperties properties = (AllowlistPluginProperties) target;

        if (properties.allowed != null) {
            properties.allowed.forEach(it -> {
                Optional<XonlyPublicKey> pubkeyOrEmpty = tryParsePublicKey(it);
                if (pubkeyOrEmpty.isEmpty()) {
                    errors.rejectValue("allowed", "allowed.invalid", "Error while parsing pubkey");
                } else {
                    if (!pubkeyOrEmpty.get().getPublicKey().isValid()) {
                        errors.rejectValue("allowed", "allowed.invalid", "Value must be a valid public key");
                    }
                }
            });
        }
    }

    private static Optional<XonlyPublicKey> tryParsePublicKey(String value) {
        try {
            return Optional.of(parsePublicKey(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // parse pubkey from hex or npub
    private static XonlyPublicKey parsePublicKey(String value) {
        return Nip19.tryDecode(value)
                .filter(it -> it.getEntityType() == Nip19Type.NPUB)
                .map(Npub.class::cast)
                .map(Npub::getPublicKey)
                .orElseGet(() -> MorePublicKeys.fromHex(value));
    }
}
