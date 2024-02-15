package org.tbk.nostr.relay.example.nostr;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface NostrSupport {
    Flux<Event> findAll(Collection<Filter> filters);
}
