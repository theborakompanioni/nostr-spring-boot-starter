package org.tbk.nostr.example.agentic;

import com.google.common.base.Strings;
import fr.acinq.bitcoin.MnemonicCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Optional;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.example.agentic",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class NostrAgenticExampleApplicationProperties implements Validator {

    @Nullable
    private IdentityProperties identity;

    private ClientProperties client;

    public Optional<IdentityProperties> getIdentity() {
        return Optional.ofNullable(identity);
    }

    public URI getRelayUri() {
        return client.getRelayUri();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == NostrAgenticExampleApplicationProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        NostrAgenticExampleApplicationProperties properties = (NostrAgenticExampleApplicationProperties) target;

        errors.pushNestedPath("client");
        ValidationUtils.invokeValidator(client, client, errors);
        errors.popNestedPath();

        properties.getIdentity().ifPresent(it -> {
            errors.pushNestedPath("identity");
            ValidationUtils.invokeValidator(it, it, errors);
            errors.popNestedPath();
        });
    }

    @Getter
    @AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
    public static class ClientProperties implements Validator {

        private String relayUri;

        public URI getRelayUri() {
            return URI.create(relayUri);
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == ClientProperties.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            ClientProperties properties = (ClientProperties) target;

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

    @Getter
    @AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
    public static class IdentityProperties implements Validator {

        private String mnemonics;

        @Nullable
        private String passphrase;

        public byte[] getSeed() {
            return MnemonicCode.toSeed(getMnemonics(), getPassphrase().orElse(""));
        }

        public Optional<String> getPassphrase() {
            return Optional.ofNullable(passphrase);
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == IdentityProperties.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            IdentityProperties properties = (IdentityProperties) target;

            String mnemonics = properties.getMnemonics();
            try {
                MnemonicCode.validate(mnemonics);
            } catch (Exception e) {
                String errorMessage = "'mnemonics' must be a valid mnemonic phrase";
                errors.rejectValue("mnemonics", "mnemonics.invalid", errorMessage);
            }
        }
    }
}
