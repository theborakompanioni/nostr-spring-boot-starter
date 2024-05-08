package org.tbk.nostr.base;

import lombok.Builder;
import lombok.Value;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * As defined in <a href="https://github.com/nostr-protocol/nips/blob/4f33dbc2b86684f9bf26dd1b0fc9789e3cbf2165/24.md">NIP-1</a>
 * and <a href="https://github.com/nostr-protocol/nips/blob/4f33dbc2b86684f9bf26dd1b0fc9789e3cbf2165/24.md">NIP-24: Extra metadata fields and tags</a>.
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
     * A boolean to clarify that the content is entirely or partially the result of automation, such as with chatbots or newsfeeds.
     */
    @Nullable
    Boolean bot;
}
