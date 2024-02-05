package org.tbk.nostr.relay.example.domain.event;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventEntities extends CrudRepository<EventEntity, EventEntity.EventEntityId>,
        PagingAndSortingRepository<EventEntity, EventEntity.EventEntityId>,
        AssociationResolver<EventEntity, EventEntity.EventEntityId> {

    Page<EventEntity> findByPubkey(@Param("pubkey") byte[] pubkey, Pageable pageable);

}