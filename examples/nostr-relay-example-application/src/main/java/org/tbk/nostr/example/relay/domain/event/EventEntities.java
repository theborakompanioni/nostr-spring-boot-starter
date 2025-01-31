package org.tbk.nostr.example.relay.domain.event;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EventEntities extends JpaRepository<EventEntity, EventEntity.EventEntityId>,
        PagingAndSortingRepository<EventEntity, EventEntity.EventEntityId>,
        JpaSpecificationExecutor<EventEntity>,
        AssociationResolver<EventEntity, EventEntity.EventEntityId> {

    default Page<EventEntity> findByPubkey(XonlyPublicKey publicKey, Pageable pageable) {
        return findAll(EventEntitySpecifications.hasPubkey(publicKey), pageable);
    }

}
