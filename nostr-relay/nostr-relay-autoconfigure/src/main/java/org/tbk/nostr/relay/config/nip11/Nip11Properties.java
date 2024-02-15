package org.tbk.nostr.relay.config.nip11;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.tbk.nostr.nip11.RelayInfoDocument;
import org.tbk.nostr.util.MorePublicKeys;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(
        prefix = "org.tbk.nostr.nip11",
        ignoreUnknownFields = false
)
@Getter
@AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
public class Nip11Properties implements Validator {

    private boolean enabled;

    private RelayInfoProperties relayInfo;


    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Nip11Properties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Nip11Properties properties = (Nip11Properties) target;

        errors.pushNestedPath("relayInfo");
        ValidationUtils.invokeValidator(properties.relayInfo, properties.relayInfo, errors);
        errors.popNestedPath();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(onConstructor = @__(@ConstructorBinding))
    public static class RelayInfoProperties implements Validator {
        /**
         * A relay may select a name for use in client software.
         * This is a string, and SHOULD be less than 30 characters to avoid client truncation.
         */
        @Nullable
        private String name;

        /**
         * Detailed plain-text information about the relay may be contained in the description string.
         * It is recommended that this contain no markup, formatting or line breaks for word wrapping, and simply use
         * double newline characters to separate paragraphs. There are no limitations on length.
         */
        @Nullable
        private String description;

        /**
         * An administrative contact may be listed with a pubkey, in the same format as Nostr events
         * (32-byte hex for a secp256k1 public key). If a contact is listed, this provides clients with a recommended
         * address to send encrypted direct messages to a system administrator. Expected uses of this address are to
         * report abuse or illegal content, file bug reports, or request other technical assistance.
         */
        @Nullable
        private String pubkey;

        /**
         * An alternative contact may be listed under the contact field as well, with the same purpose as pubkey.
         * Use of a Nostr public key and direct message SHOULD be preferred over this. Contents of this field SHOULD be a
         * URI, using schemes such as mailto or https to provide users with a means of contact.
         */
        @Nullable
        private String contact;

        /**
         * As the Nostr protocol evolves, some functionality may only be available by relays that implement a specific NIP.
         * This field is an array of the integer identifiers of NIPs that are implemented in the relay. Client-side NIPs
         * SHOULD NOT be advertised, and can be ignored by clients.
         */
        @Nullable
        private List<Integer> supportedNips;

        /**
         * The relay server implementation MAY be provided in the software attribute.
         * If present, this MUST be a URL to the project's homepage.
         */
        @Nullable
        private String software;

        /**
         * The relay MAY choose to publish its software version as a string attribute. The string format is defined by the
         * relay implementation. It is recommended this be a version number or commit identifier.
         */
        @Nullable
        private String version;

        @Nullable
        public XonlyPublicKey getPubkey() {
            return pubkey == null ? null : MorePublicKeys.fromHex(this.pubkey);
        }

        @Nullable
        public URI getSoftware() {
            return software == null ? null : URI.create(this.software);
        }

        public List<Integer> getSupportedNips() {
            return supportedNips == null ? Collections.emptyList() : Collections.unmodifiableList(supportedNips);
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return clazz == RelayInfoProperties.class;
        }

        @Override
        public void validate(Object target, Errors errors) {
            RelayInfoProperties properties = (RelayInfoProperties) target;

            if (properties.name != null) {
                if (properties.name.isBlank()) {
                    String errorMessage = "'name' must not be blank";
                    errors.rejectValue("name", "name.invalid", errorMessage);
                }
                if (properties.name.length() > 30) {
                    String errorMessage = "'name' must not be longer than 30 chars";
                    errors.rejectValue("name", "name.invalid", errorMessage);
                }
            }

            if (properties.pubkey != null) {
                try {
                    XonlyPublicKey ignoredOnPurpose = MorePublicKeys.fromHex(properties.pubkey);
                } catch (IllegalArgumentException e) {
                    String errorMessage = "'pubkey' must be a 32-byte hex string of a secp256k1 public key";
                    errors.rejectValue("pubkey", "pubkey.invalid", errorMessage);
                }
            }

            if (properties.software != null) {
                try {
                    URI ignoredOnPurpose = URI.create(properties.software);
                } catch (IllegalArgumentException e) {
                    String errorMessage = "'software' must be a valid URI";
                    errors.rejectValue("software", "software.invalid", errorMessage);
                }
            }
        }

        public RelayInfoDocument.Builder toRelayInfoDocument() {
            return RelayInfoDocument.newBuilder()
                    .name(this.getName())
                    .description(this.getDescription())
                    .pubkey(this.getPubkey())
                    .contact(this.getContact())
                    .supportedNips(this.getSupportedNips())
                    .software(this.getSoftware())
                    .version(this.getVersion());
        }
    }
}
