package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.PrivateKey;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Nsec implements Nip19Entity {
    @NonNull
    PrivateKey privateKey;
    
    @Override
    public Nip19Type getEntityType() {
        return Nip19Type.NSEC;
    }
}
