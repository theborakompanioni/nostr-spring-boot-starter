package org.tbk.nostr.relay.plugin.allowlist.db.domain;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AllowlistEntries extends JpaRepository<AllowlistEntry, AllowlistEntry.AllowlistEntryId>,
        PagingAndSortingRepository<AllowlistEntry, AllowlistEntry.AllowlistEntryId>,
        JpaSpecificationExecutor<AllowlistEntry>,
        AssociationResolver<AllowlistEntry, AllowlistEntry.AllowlistEntryId> {

    default Page<AllowlistEntry> findByPubkey(XonlyPublicKey publicKey, Pageable pageable) {
        return findAll(AllowlistEntrySpecifications.hasPubkey(publicKey), pageable);
    }

    // this is used in favor of "CASE WHEN COUNT(1) = 0 THEN TRUE ELSE FALSE END" as a workaround for sqlite
    @Query(value = "SELECT COUNT(1) FROM plugin_allowlist_entry LIMIT 1", nativeQuery = true)
    int __hasEntriesAsInt();

    default boolean isEmpty() {
        return __hasEntriesAsInt() == 0;
    }
}
