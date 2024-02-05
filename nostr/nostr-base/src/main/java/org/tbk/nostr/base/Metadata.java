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
}
