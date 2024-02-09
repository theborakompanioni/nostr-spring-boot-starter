package org.tbk.nostr.nip11;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.tbk.nostr.util.MorePublicKeys;

import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * {
 * "name": <string identifying relay>,
 * "description": <string with detailed information>,
 * "pubkey": <administrative contact pubkey>,
 * "contact": <administrative alternate contact>,
 * "supported_nips": <a list of NIP numbers supported by the relay>,
 * "software": <string identifying relay software URL>,
 * "version": <string version identifier>
 * }
 */
@Value
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class RelayInfoDocument {
    private static final JSON json = JSON.std
            .without(JSON.Feature.WRITE_NULL_PROPERTIES);

    /**
     * A relay may select a name for use in client software.
     * This is a string, and SHOULD be less than 30 characters to avoid client truncation.
     */
    String name;

    /**
     * Detailed plain-text information about the relay may be contained in the description string.
     * It is recommended that this contain no markup, formatting or line breaks for word wrapping, and simply use
     * double newline characters to separate paragraphs. There are no limitations on length.
     */
    String description;

    /**
     * An administrative contact may be listed with a pubkey, in the same format as Nostr events
     * (32-byte hex for a secp256k1 public key). If a contact is listed, this provides clients with a recommended
     * address to send encrypted direct messages to a system administrator. Expected uses of this address are to
     * report abuse or illegal content, file bug reports, or request other technical assistance.
     */
    XonlyPublicKey pubkey;

    /**
     * An alternative contact may be listed under the contact field as well, with the same purpose as pubkey.
     * Use of a Nostr public key and direct message SHOULD be preferred over this. Contents of this field SHOULD be a
     * URI, using schemes such as mailto or https to provide users with a means of contact.
     */
    String contact;

    /**
     * As the Nostr protocol evolves, some functionality may only be available by relays that implement a specific NIP.
     * This field is an array of the integer identifiers of NIPs that are implemented in the relay. Client-side NIPs
     * SHOULD NOT be advertised, and can be ignored by clients.
     */
    @Singular("addSupportedNip")
    List<Integer> supportedNips;

    /**
     * The relay server implementation MAY be provided in the software attribute.
     * If present, this MUST be a URL to the project's homepage.
     */
    URI software;

    /**
     * The relay MAY choose to publish its software version as a string attribute. The string format is defined by the
     * relay implementation. It is recommended this be a version number or commit identifier.
     */
    String version;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<XonlyPublicKey> getPubkey() {
        return Optional.ofNullable(pubkey);
    }

    public Optional<String> getContact() {
        return Optional.ofNullable(contact);
    }

    public Optional<URI> getSoftware() {
        return Optional.ofNullable(software);
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(version);
    }

    public String toJson() {
        return RelayInfoDocument.toJson(this);
    }

    public static String toJson(RelayInfoDocument doc) {
        try {
            ObjectComposer<JSONComposer<String>> jsonComposer = json.composeString().startObject();

            doc.getName().ifPresent(it -> put(jsonComposer, "name", it));
            doc.getDescription().ifPresent(it -> put(jsonComposer, "description", it));
            doc.getPubkey().ifPresent(it -> put(jsonComposer, "pubkey", it.value.toHex()));
            doc.getContact().ifPresent(it -> put(jsonComposer, "contact", it));
            jsonComposer.putObject("supported_nips", doc.getSupportedNips());
            doc.getSoftware().ifPresent(it -> put(jsonComposer, "software", it));
            doc.getVersion().ifPresent(it -> put(jsonComposer, "version", it));
            return jsonComposer.end().finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RelayInfoDocument fromJson(String value) {
        return fromJson(value, RelayInfoDocument.newBuilder());
    }

    static RelayInfoDocument fromJson(String val, RelayInfoDocument.Builder builder) {
        try {
            return fromMap(json.mapFrom(val), builder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static RelayInfoDocument fromMap(Map<String, Object> map, Builder builder) {
        Object name = map.get("name");
        Object description = map.get("description");
        Object pubkey = map.get("pubkey");
        Object contact = map.get("contact");
        @SuppressWarnings("unchecked")
        Collection<Object> supportedNips = (Collection<Object>) map.get("supported_nips");
        Object software = map.get("software");
        Object version = map.get("version");

        return builder
                .name(name == null ? null : String.valueOf(name))
                .description(description == null ? null : String.valueOf(description) )
                .pubkey(pubkey == null ? null : MorePublicKeys.fromHex(String.valueOf(pubkey)))
                .contact(contact == null ? null : String.valueOf(contact))
                .supportedNips(supportedNips == null ? Collections.emptyList() : supportedNips.stream()
                        .mapToInt(it -> Integer.parseInt(String.valueOf(it)))
                        .boxed().toList())
                .software(software == null ? null : URI.create(String.valueOf(software)))
                .version(version == null ? null : String.valueOf(version))
                .build();
    }

    private static void put(ObjectComposer<JSONComposer<String>> jsonComposer, String name, Object value) {
        try {
            jsonComposer.putObject(name, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
