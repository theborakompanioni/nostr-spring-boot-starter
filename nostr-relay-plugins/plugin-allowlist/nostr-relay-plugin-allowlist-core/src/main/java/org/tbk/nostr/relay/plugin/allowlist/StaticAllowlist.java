package org.tbk.nostr.relay.plugin.allowlist;

import fr.acinq.bitcoin.XonlyPublicKey;

import java.util.Collections;
import java.util.List;

public class StaticAllowlist implements Allowlist {

    private final List<XonlyPublicKey> allowed;

    public StaticAllowlist(List<XonlyPublicKey> allowed) {
        this.allowed = Collections.unmodifiableList(allowed);
    }

    @Override
    public boolean isAllowed(XonlyPublicKey pubkey) {
        return allowed.contains(pubkey);
    }
}
