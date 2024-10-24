package org.tbk.nostr.nip11;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.tbk.nostr.util.MorePublicKeys;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Primary fields
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
    @Nullable
    String name;

    /**
     * Detailed plain-text information about the relay may be contained in the description string.
     * It is recommended that this contain no markup, formatting or line breaks for word wrapping, and simply use
     * double newline characters to separate paragraphs. There are no limitations on length.
     */
    @Nullable
    String description;

    /**
     * An administrative contact may be listed with a pubkey, in the same format as Nostr events
     * (32-byte hex for a secp256k1 public key). If a contact is listed, this provides clients with a recommended
     * address to send encrypted direct messages to a system administrator. Expected uses of this address are to
     * report abuse or illegal content, file bug reports, or request other technical assistance.
     */
    @Nullable
    XonlyPublicKey pubkey;

    /**
     * An alternative contact may be listed under the contact field as well, with the same purpose as pubkey.
     * Use of a Nostr public key and direct message SHOULD be preferred over this. Contents of this field SHOULD be a
     * URI, using schemes such as mailto or https to provide users with a means of contact.
     */
    @Nullable
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
    @Nullable
    URI software;

    /**
     * The relay MAY choose to publish its software version as a string attribute. The string format is defined by the
     * relay implementation. It is recommended this be a version number or commit identifier.
     */
    @Nullable
    String version;

    /**
     * A URL pointing to an image to be used as an icon for the relay. Recommended to be squared in shape.
     * @apiNote Extra field.
     */
    @Nullable
    String icon;

    public String toJson() {
        return RelayInfoDocument.toJson(this);
    }

    public static String toJson(RelayInfoDocument doc) {
        try {
            ObjectComposer<JSONComposer<String>> jsonComposer = json.composeString().startObject();

            if (doc.name != null) {
                jsonComposer.put("name", doc.name);
            }
            if (doc.description != null) {
                jsonComposer.put("description", doc.description);
            }
            if (doc.pubkey != null) {
                jsonComposer.put("pubkey", doc.pubkey.value.toHex());
            }
            if (doc.contact != null) {
                jsonComposer.put("contact", doc.contact);
            }

            jsonComposer.putObject("supported_nips", doc.getSupportedNips());

            if (doc.software != null) {
                jsonComposer.put("software", doc.software.toString());
            }
            if (doc.version != null) {
                jsonComposer.put("version", doc.version);
            }
            if (doc.icon != null) {
                jsonComposer.put("icon", doc.icon);
            }
            return jsonComposer.end().finish();
        } catch (Exception e) {
            throw new RuntimeException("Error while serializing object to json", e);
        }
    }

    public static RelayInfoDocument fromJson(String value) {
        return fromJson(value, RelayInfoDocument.newBuilder());
    }

    static RelayInfoDocument fromJson(String val, RelayInfoDocument.Builder builder) {
        try {
            return fromMap(json.mapFrom(val), builder);
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing object from json", e);
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
        Object icon = map.get("icon");

        return builder
                .name(name == null ? null : String.valueOf(name))
                .description(description == null ? null : String.valueOf(description))
                .pubkey(pubkey == null ? null : MorePublicKeys.fromHex(String.valueOf(pubkey)))
                .contact(contact == null ? null : String.valueOf(contact))
                .supportedNips(supportedNips == null ? Collections.emptyList() : supportedNips.stream()
                        .mapToInt(it -> Integer.parseInt(String.valueOf(it)))
                        .boxed().toList())
                .software(software == null ? null : URI.create(String.valueOf(software)))
                .version(version == null ? null : String.valueOf(version))
                .icon(icon == null ? null : String.valueOf(icon))
                .build();
    }
}
