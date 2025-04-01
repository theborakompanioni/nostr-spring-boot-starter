package org.tbk.nostr.relay.plugin.allowlist.db.domain;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllowlistEntryService {

    @NonNull
    private final AllowlistEntries entries;

    @Transactional
    public boolean hasEntries() {
        return entries.count() != 0L;
    }

    @Transactional
    public void create(XonlyPublicKey publicKey) {
        AllowlistEntry entry = new AllowlistEntry(publicKey);
        entries.save(entry);
    }

    @Transactional
    public Page<AllowlistEntry> findByPubkey(XonlyPublicKey publicKey) {
        return entries.findByPubkey(publicKey, PageRequest.ofSize(1));
    }

    @Transactional
    public Optional<AllowlistEntry> findFirstByPubkey(XonlyPublicKey publicKey) {
        return findByPubkey(publicKey).stream().findFirst();
    }

    @Transactional
    public void remove(XonlyPublicKey publicKey) {
        entries.delete(AllowlistEntrySpecifications.hasPubkey(publicKey));
    }
}
