package org.tbk.nostr.relay.example.domain.event;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface EventEntities extends JpaRepository<EventEntity, EventEntity.EventEntityId>,
        PagingAndSortingRepository<EventEntity, EventEntity.EventEntityId>,
        JpaSpecificationExecutor<EventEntity>,
        AssociationResolver<EventEntity, EventEntity.EventEntityId> {

    Page<EventEntity> findByPubkey(@Param("pubkey") byte[] pubkey, Pageable pageable);

}