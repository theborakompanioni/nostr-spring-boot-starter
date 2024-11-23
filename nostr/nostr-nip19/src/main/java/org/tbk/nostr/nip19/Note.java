package org.tbk.nostr.nip19;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.tbk.nostr.base.EventId;

@Value
@Builder
public class Note implements Nip19Entity {
    @NonNull
    EventId eventId;

    @Override
    public Nip19Type getEntityType() {
        return Nip19Type.NOTE;
    }
}
