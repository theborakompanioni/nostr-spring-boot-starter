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
import org.tbk.nostr.proto.ProfileMetadata;

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

    @Nullable
    private ProfileMetadataProperties profileMetadata;

    private ClientProperties client;

    public Optional<IdentityProperties> getIdentity() {
        return Optional.ofNullable(identity);
    }

    public Optional<ProfileMetadataProperties> getProfileMetadata() {
        return Optional.ofNullable(profileMetadata);
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
        properties.getProfileMetadata().ifPresent(it -> {
            errors.pushNestedPath("profileMetadata");
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
    // TODO: ReadWriteRelays

    // TODO: split into content and tags
    @Getter
    @AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
    public static class ProfileMetadataProperties implements Validator {
        String name;
        String about;
        String picture;
        /**
         * An alternative, bigger name with richer characters than `name`.
         * `name` should always be set regardless of the presence of `display_name` in the metadata.
         */
        String displayName;
        /**
         * A web URL related in any way to the event author.
         */
        String website;
        /**
         * A web URL to a wide (~1024x768) picture to be optionally displayed in the background of a profile screen.
         */
        String banner;
        /**
         * A boolean to clarify that the content is entirely or partially the result of automation, such as with chatbots
         * or newsfeeds.
         */
        Boolean bot;
        /**
         * An <a href="https://datatracker.ietf.org/doc/html/rfc5322#section-3.4.1">internet identifier</a>
         * (an email-like address) as the value. Although there is a link to a very liberal "internet identifier"
         * specification above, NIP-05 assumes the <local-part> part will be restricted to the characters
         * <code>a-z0-9-_.</code>, case-insensitive.
         */
        String nip05;

        String lud16;

        Optional<String> getName() {
            return Optional.ofNullable(name);
        }

        Optional<String> getAbout() {
            return Optional.ofNullable(about);
        }

        Optional<String> getPicture() {
            return Optional.ofNullable(picture);
        }

        Optional<String> getDisplayName() {
            return Optional.ofNullable(displayName);
        }

        Optional<String> getWebsite() {
            return Optional.ofNullable(website);
        }

        Optional<String> getBanner() {
            return Optional.ofNullable(banner);
        }

        Optional<Boolean> getBot() {
            return Optional.ofNullable(bot);
        }

        Optional<String> getNip05() {
            return Optional.ofNullable(nip05);
        }

        Optional<String> getLud16() {
            return Optional.ofNullable(lud16);
        }

        public ProfileMetadata toProfileMetadata() {
            ProfileMetadata.Builder builder = ProfileMetadata.newBuilder();
            this.getName().ifPresent(builder::setName);
            this.getAbout().ifPresent(builder::setAbout);
            this.getPicture().ifPresent(builder::setPicture);
            this.getDisplayName().ifPresent(builder::setDisplayName);
            this.getWebsite().ifPresent(builder::setWebsite);
            this.getBanner().ifPresent(builder::setBanner);
            this.getBot().ifPresent(builder::setBot);
            this.getNip05().ifPresent(builder::setNip05);
            this.getLud16().ifPresent(builder::setLud16);
            return builder.build();
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == ProfileMetadataProperties.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            ProfileMetadataProperties properties = (ProfileMetadataProperties) target;

            String website = properties.getWebsite().orElse(null);
            if (website != null) {
                if (!website.startsWith("http://") && !website.startsWith("https://")) {
                    String errorMessage = "'website' must start with 'ws://' or 'wss://'";
                    errors.rejectValue("website", "website.invalid", errorMessage);
                } else {
                    try {
                        @SuppressWarnings("unused")
                        var unusedOnPurpose = URI.create(website);
                    } catch (IllegalArgumentException e) {
                        String errorMessage = "'website' must be a valid URI";
                        errors.rejectValue("website", "website.invalid", errorMessage);
                    }
                }
            }
        }
    }
}
