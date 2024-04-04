package org.tbk.nostr.example.relay.domain.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.nostr.example.relay.NostrRelayExampleApplicationProperties;
import org.tbk.nostr.example.relay.impl.nip50.Nip50SearchTerm;
import org.tbk.nostr.proto.Filter;

@Slf4j
@Transactional
class PostgresEventEntityService extends AbstractEventEntityService {

    PostgresEventEntityService(EventEntities events, NostrRelayExampleApplicationProperties properties) {
        super(events, properties);
    }

    @Override
    protected Specification<EventEntity> search(Filter filter) {
        return Nip50SearchTerm.from(filter)
                .map(EventEntitySpecifications::searchPostgres)
                .orElse(Specification.where(null));
    }

}
