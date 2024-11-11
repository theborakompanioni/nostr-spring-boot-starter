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

    Flux<Event> fetchEvents(ReqRequest request);

    Mono<Event> fetchEventById(EventId id);

    Flux<Event> fetchEventsByIds(Collection<EventId> ids);

    Flux<Event> fetchEventByAuthor(XonlyPublicKey publicKey);

    Flux<Event> fetchEventsByAuthors(Collection<XonlyPublicKey> publicKeys);

    Mono<Metadata> fetchMetadataByAuthor(XonlyPublicKey publicKey);

    Flux<CountResult> countEvents(CountRequest request);

    Mono<OkResponse> send(Event event);

    Flux<OkResponse> send(Collection<Event> events);

    /**
     * A helper function for sending an arbitrary plain string.
     *
     * @param message the message content (possibly json)
     * @return responses from the relay
     */
    Flux<Response> sendPlain(String message);

    /**
     * A helper function for sending arbitrary plain strings.
     *
     * @param messages the messages contents (possibly json)
     * @return responses from the relay
     * @see #sendPlain(String)
     */
    Flux<Response> sendPlain(Collection<String> messages);

    /**
     * A helper function for sending a plain string returning the first response.
     * Useful for testing relay behaviour for malformed messages.
     *
     * @param message the message content (possibly json)
     * @return the first response from the relay
     */
    Mono<Response> sendPlainMono(String message);
}
