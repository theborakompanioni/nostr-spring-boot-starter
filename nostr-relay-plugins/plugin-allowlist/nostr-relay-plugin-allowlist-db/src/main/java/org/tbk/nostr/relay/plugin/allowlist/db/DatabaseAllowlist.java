package org.tbk.nostr.relay.plugin.allowlist.db;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.RequiredArgsConstructor;
import org.tbk.nostr.relay.plugin.allowlist.Allowlist;
import org.tbk.nostr.relay.plugin.allowlist.db.domain.AllowlistEntry;
import org.tbk.nostr.relay.plugin.allowlist.db.domain.AllowlistEntryService;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class DatabaseAllowlist implements Allowlist {

    private final AllowlistEntryService allowlistEntryService;

    @Override
    public boolean isAllowed(XonlyPublicKey pubkey) {
        Optional<AllowlistEntry> allowlistEntry = allowlistEntryService.findFirstByPubkey(pubkey);

        if (allowlistEntry.isEmpty()) {
            // TODO: caching
            if (!allowlistEntryService.hasEntries()) {
                return true;
            }
        }
        return allowlistEntry
                .filter(it -> !it.isExpired(Instant.now()))
                .isPresent();
    }
}
