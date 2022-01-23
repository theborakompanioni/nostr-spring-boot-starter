package org.tbk.nostr.template;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.ReqRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface NostrTemplate {
    Mono<Event> fetchEventById(EventId id);

    Flux<Event> fetchEventsByIds(List<EventId> ids);

    Flux<Event> fetchEventByAuthor(XonlyPublicKey publicKey);

    Flux<Event> fetchEventsByAuthors(List<XonlyPublicKey> publicKeys);

    Flux<Event> fetchEvents(ReqRequest request);

    Mono<OkResponse> send(Event event);
}
