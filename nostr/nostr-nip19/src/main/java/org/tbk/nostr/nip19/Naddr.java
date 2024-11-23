package org.tbk.nostr.nip19;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.RelayUri;

import java.util.List;

@Value
@Builder
public class Naddr implements Nip19Entity {
    @NonNull
    EventUri eventUri;

    @Singular("relay")
    List<RelayUri> relays;

    @Override
    public Nip19Type getEntityType() {
        return Nip19Type.NADDR;
    }
}
