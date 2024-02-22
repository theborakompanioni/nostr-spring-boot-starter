package org.tbk.nostr.example.relay;

import fr.acinq.bitcoin.MnemonicCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import javax.annotation.Nullable;
import java.util.Optional;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.example.relay",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class NostrRelayExampleApplicationProperties implements Validator {

    private static final int INITIAL_QUERY_LIMIT_DEFAULT = 100;

    private static final AsyncExecutorProperties DEFAULT_ASYNC_EXECUTOR = new AsyncExecutorProperties();

    @Nullable
    private IdentityProperties identity;

    @Nullable
    private Boolean startupEventsEnabled;

    // initial message sent to users on websocket connection established
    @Nullable
    private String greeting;

    @Nullable
    private Integer initialQueryLimit;

    @Nullable
    private AsyncExecutorProperties asyncExecutor;

    public Optional<IdentityProperties> getIdentity() {
        return Optional.ofNullable(identity);
    }

    public Optional<Boolean> getStartupEventsEnabled() {
        return Optional.ofNullable(startupEventsEnabled);
    }

    public Optional<String> getGreeting() {
        return Optional.ofNullable(greeting).filter(it -> !it.isBlank());
    }

    public int getInitialQueryLimit() {
        return Optional.ofNullable(initialQueryLimit).orElse(INITIAL_QUERY_LIMIT_DEFAULT);
    }


    public AsyncExecutorProperties getAsyncExecutor() {
        return Optional.ofNullable(asyncExecutor).orElse(DEFAULT_ASYNC_EXECUTOR);
    }

    public boolean isStartupEventsEnabled() {
        return getStartupEventsEnabled().orElse(true);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == NostrRelayExampleApplicationProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        NostrRelayExampleApplicationProperties properties = (NostrRelayExampleApplicationProperties) target;

        if (properties.isStartupEventsEnabled() && properties.getIdentity().isEmpty()) {
            String errorMessage = "'startupEventsEnabled' cannot be used if 'identity' is empty";
            errors.rejectValue("startupEventsEnabled", "startupEventsEnabled.invalid", errorMessage);
        }
        if (properties.getInitialQueryLimit() <= 0) {
            String errorMessage = "'initialQueryLimit' must be greater than zero";
            errors.rejectValue("initialQueryLimit", "initialQueryLimit.invalid", errorMessage);
        }

        properties.getIdentity().ifPresent(it -> {
            errors.pushNestedPath("identity");
            ValidationUtils.invokeValidator(it, it, errors);
            errors.popNestedPath();
        });
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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
    public static class AsyncExecutorProperties implements Validator {

        private static final int MAX_POOL_SIZE_DEFAULT = 1;

        private Integer maxPoolSize;

        public int getMaxPoolSize() {
            return Optional.ofNullable(maxPoolSize).orElse(MAX_POOL_SIZE_DEFAULT);
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == AsyncExecutorProperties.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            AsyncExecutorProperties properties = (AsyncExecutorProperties) target;

            if (properties.getMaxPoolSize() <= 0) {
                String errorMessage = "'maxPoolSize' must be greater than zero";
                errors.rejectValue("maxPoolSize", "maxPoolSize.invalid", errorMessage);
            }
        }
    }
}
