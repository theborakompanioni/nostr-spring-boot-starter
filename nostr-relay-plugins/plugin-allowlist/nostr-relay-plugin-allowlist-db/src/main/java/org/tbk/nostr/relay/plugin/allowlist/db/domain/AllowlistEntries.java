package org.tbk.nostr.relay.plugin.allowlist.db.domain;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AllowlistEntries extends JpaRepository<AllowlistEntry, AllowlistEntry.AllowlistEntryId>,
        PagingAndSortingRepository<AllowlistEntry, AllowlistEntry.AllowlistEntryId>,
        JpaSpecificationExecutor<AllowlistEntry>,
        AssociationResolver<AllowlistEntry, AllowlistEntry.AllowlistEntryId> {

    default Page<AllowlistEntry> findByPubkey(XonlyPublicKey publicKey, Pageable pageable) {
        return findAll(AllowlistEntrySpecifications.hasPubkey(publicKey), pageable);
    }
}
