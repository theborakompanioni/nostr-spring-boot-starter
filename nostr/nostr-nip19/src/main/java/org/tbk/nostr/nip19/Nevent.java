package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;

import java.util.List;
import java.util.Optional;

@Value
@Builder
public class Nevent implements Nip19Entity {
    @NonNull
    EventId eventId;

    @Singular("relay")
    List<RelayUri> relays;

    XonlyPublicKey publicKey;

    Kind kind;

    @Override
    public Nip19Type getEntityType() {
        return Nip19Type.NEVENT;
    }

    public Optional<XonlyPublicKey> getPublicKey() {
        return Optional.ofNullable(publicKey);
    }

    public Optional<Kind> getKind() {
        return Optional.ofNullable(kind);
    }
}
