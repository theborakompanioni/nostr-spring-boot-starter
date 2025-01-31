package org.tbk.nostr.relay.plugin.allowlist;

import fr.acinq.bitcoin.XonlyPublicKey;

public interface Allowlist {

    boolean isAllowed(XonlyPublicKey pubkey);

}
