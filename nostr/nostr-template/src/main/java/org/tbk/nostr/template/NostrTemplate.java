package org.tbk.nostr.template;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface NostrTemplate {
    Mono<OkResponse> send(Event event);

    Mono<Event> fetchEventById(EventId id);

    Flux<Event> fetchEventsByIds(List<EventId> ids);

    Flux<Event> fetchEventByAuthor(XonlyPublicKey publicKey);

    Flux<Event> fetchEventsByAuthors(List<XonlyPublicKey> publicKeys);

    Mono<Metadata> fetchMetadataByAuthor(XonlyPublicKey publicKey);

    Flux<Event> fetchEvents(ReqRequest request);

    Flux<CountResult> countEvents(CountRequest request);
}
