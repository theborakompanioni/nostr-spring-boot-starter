package org.tbk.nostr.relay.plugin.allowlist;

import fr.acinq.bitcoin.XonlyPublicKey;

public class EmptyAllowlist implements Allowlist {

    @Override
    public boolean isAllowed(XonlyPublicKey pubkey) {
        return true;
    }
}
