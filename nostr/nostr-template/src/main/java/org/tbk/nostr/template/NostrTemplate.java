package org.tbk.nostr.template;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.nip11.RelayInfoDocument;
import org.tbk.nostr.proto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collection;

public interface NostrTemplate {
    Mono<RelayInfoDocument> fetchRelayInfoDocument();

    Mono<RelayInfoDocument> fetchRelayInfoDocument(URI uri);

    Mono<OkResponse> send(Event event);

    Flux<OkResponse> send(Collection<Event> events);

    Mono<Event> fetchEventById(EventId id);

    Flux<Event> fetchEventsByIds(Collection<EventId> ids);

    Flux<Event> fetchEventByAuthor(XonlyPublicKey publicKey);

    Flux<Event> fetchEventsByAuthors(Collection<XonlyPublicKey> publicKeys);

    Mono<Metadata> fetchMetadataByAuthor(XonlyPublicKey publicKey);

    Flux<Event> fetchEvents(ReqRequest request);

    Flux<CountResult> countEvents(CountRequest request);
}
