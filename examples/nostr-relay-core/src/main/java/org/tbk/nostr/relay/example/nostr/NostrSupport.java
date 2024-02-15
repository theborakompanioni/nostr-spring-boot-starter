package org.tbk.nostr.relay.example.nostr;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.OkResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface NostrSupport {
    
    Mono<OkResponse> createEvent(Event event);

    Flux<Event> findAll(Collection<Filter> filters);
}
