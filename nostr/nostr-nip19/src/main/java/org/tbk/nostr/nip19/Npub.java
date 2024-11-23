package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Npub implements Nip19Entity {
    @NonNull
    XonlyPublicKey publicKey;

    @Override
    public Nip19Type getEntityType() {
        return Nip19Type.NPUB;
    }
}
