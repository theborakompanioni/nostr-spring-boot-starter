package org.tbk.nostr.relay.example.domain.event;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import reactor.core.publisher.Flux;

import java.util.List;

public interface EventEntityService {

    EventEntity createEvent(Event event);

    Flux<EventEntity> find(List<Filter> filters);
}
