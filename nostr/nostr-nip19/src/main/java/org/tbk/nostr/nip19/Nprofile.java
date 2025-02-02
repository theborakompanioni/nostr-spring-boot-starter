package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.tbk.nostr.base.RelayUri;

import java.util.List;

@Value
@Builder
public class Nprofile implements Nip19Entity {
    @NonNull
    XonlyPublicKey publicKey;

    @Singular("relay")
    List<RelayUri> relays;

    @Override
    public Nip19Type getEntityType() {
        return Nip19Type.NPROFILE;
    }
}
