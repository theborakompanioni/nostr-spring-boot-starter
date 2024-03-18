package org.tbk.nostr.base;

import lombok.Builder;
import lombok.Value;

import javax.annotation.Nullable;
import java.net.URI;

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
}
