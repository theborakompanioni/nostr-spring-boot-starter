package org.tbk.nostr.relay.example;

import fr.acinq.bitcoin.MnemonicCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Nullable;
import java.util.Optional;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.relay.example",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class NostrRelayExampleApplicationProperties implements Validator {

    private IdentityProperties identity;

    // initial message sent to users on websocket connection established
    private String greeting;

    private Boolean startupEventsEnabled;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == NostrRelayExampleApplicationProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        NostrRelayExampleApplicationProperties properties = (NostrRelayExampleApplicationProperties) target;

        // empty on purpose
    }

    public Optional<String> getGreeting() {
        return Optional.ofNullable(greeting);
    }

    public Optional<Boolean> getStartupEventsEnabled() {
        return Optional.ofNullable(startupEventsEnabled);
    }

    public boolean isStartupEventsEnabled() {
        return getStartupEventsEnabled().orElse(true);
    }

    @Getter
    @AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
    public static class IdentityProperties implements Validator {

        private String mnemonics;

        @Nullable
        private String passphrase;


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

        public byte[] getSeed() {
            return MnemonicCode.toSeed(getMnemonics(), getPassphrase().orElse(""));
        }

        public Optional<String> getPassphrase() {
            return Optional.ofNullable(passphrase);
        }
    }

}
