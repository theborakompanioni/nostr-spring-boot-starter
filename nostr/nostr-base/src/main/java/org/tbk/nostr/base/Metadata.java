package org.tbk.nostr.base;

import lombok.Builder;
import lombok.Value;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * As defined in <a href="https://github.com/nostr-protocol/nips/blob/4f33dbc2b86684f9bf26dd1b0fc9789e3cbf2165/01.md">NIP-1</a>,
 * <a href="https://github.com/nostr-protocol/nips/blob/4f33dbc2b86684f9bf26dd1b0fc9789e3cbf2165/05.md">NIP-05</a> and
 * <a href="https://github.com/nostr-protocol/nips/blob/4f33dbc2b86684f9bf26dd1b0fc9789e3cbf2165/24.md">NIP-24</a>.
 */
@Value
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class Metadata {
    @Nullable
    String name;

    @Nullable
    String about;

    @Nullable
    URI picture;

    /**
     * An alternative, bigger name with richer characters than `name`.
     * `name` should always be set regardless of the presence of `display_name` in the metadata.
     */
    @Nullable
    String displayName;

    /**
     * A web URL related in any way to the event author.
     */
    @Nullable
    URI website;

    /**
     * A web URL to a wide (~1024x768) picture to be optionally displayed in the background of a profile screen.
     */
    @Nullable
    URI banner;

    /**
     * A boolean to clarify that the content is entirely or partially the result of automation, such as with chatbots
     * or newsfeeds.
     */
    @Nullable
    Boolean bot;

    /**
     * An <a href="https://datatracker.ietf.org/doc/html/rfc5322#section-3.4.1">internet identifier</a>
     * (an email-like address) as the value. Although there is a link to a very liberal "internet identifier"
     * specification above, NIP-05 assumes the <local-part> part will be restricted to the characters
     * <code>a-z0-9-_.</code>, case-insensitive.
     */
    @Nullable
    String nip05;

    @Nullable
    String lud16;
}
