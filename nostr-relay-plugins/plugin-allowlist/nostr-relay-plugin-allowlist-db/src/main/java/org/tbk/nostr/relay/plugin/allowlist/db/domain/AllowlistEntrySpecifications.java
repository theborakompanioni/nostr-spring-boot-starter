package org.tbk.nostr.relay.plugin.allowlist.db.domain;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.springframework.data.jpa.domain.Specification;

public final class AllowlistEntrySpecifications {

    public static Specification<AllowlistEntry> hasPubkey(XonlyPublicKey pubkey) {
        return (root, cq, cb) -> cb.equal(root.get("pubkey"), pubkey.value.toHex());
    }

    public static Specification<AllowlistEntry> hasPubkey(ByteString pubkey) {
        return hasPubkey(new XonlyPublicKey(new ByteVector32(pubkey.toByteArray())));
    }
}
