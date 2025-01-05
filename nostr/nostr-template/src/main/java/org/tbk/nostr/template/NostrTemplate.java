package org.tbk.nostr.template;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nip11.RelayInfoDocument;
import org.tbk.nostr.nips.Nip65;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public interface NostrTemplate {
    RelayUri getRelayUri();

    Mono<RelayInfoDocument> fetchRelayInfoDocument();

    Mono<RelayInfoDocument> fetchRelayInfoDocument(URI uri);

    Flux<Response> fetch(ReqRequest request);

    Flux<Event> fetchEvents(ReqRequest request);

    Mono<Event> fetchEventById(EventId id);

    Flux<Event> fetchEventsByIds(Collection<EventId> ids);

    Flux<Event> fetchEventsByAuthor(XonlyPublicKey publicKey);

    Flux<Event> fetchEventsByAuthors(Collection<XonlyPublicKey> publicKeys);

    Mono<Metadata> fetchMetadataByAuthor(XonlyPublicKey publicKey);

    Mono<Nip65.ReadWriteRelays> fetchRelayListByAuthor(XonlyPublicKey publicKey);

    Flux<Response> count(CountRequest request);

    Flux<CountResult> countEvents(CountRequest request);

    Mono<OkResponse> auth(Event event);

    default Mono<OkResponse> send(Event event) {
        return send(Collections.singleton(event)).next();
    }

    Flux<OkResponse> send(Collection<Event> events);

    default Flux<Response> publish(Request request) {
        return publishPlain(JsonWriter.toJson(request));
    }

    default Flux<Response> publish(Collection<Request> requests) {
        return publishPlain(requests.stream().map(JsonWriter::toJson).toList());
    }

    default Flux<Response> publishEvent(Event event) {
        return publishEvents(Collections.singleton(event));
    }

    default Flux<Response> publishEvents(Collection<Event> events) {
        return publish(events.stream()
                .map(it -> Request.newBuilder()
                        .setEvent(EventRequest.newBuilder()
                                .setEvent(it)
                                .build())
                        .build())
                .toList());
    }

    /**
     * A helper function for sending an arbitrary plain string.
     *
     * @param message the message content (possibly json)
     * @return responses from the relay
     */
    default Flux<Response> publishPlain(String message) {
        return publishPlain(Collections.singletonList(message));
    }

    /**
     * A helper function for sending arbitrary plain strings.
     *
     * @param messages the messages contents (possibly json)
     * @return responses from the relay
     * @see #publishPlain(String)
     */
    Flux<Response> publishPlain(Collection<String> messages);

    /**
     * A helper function for sending a plain string returning the first response.
     * Useful for testing relay behaviour for malformed messages.
     *
     * @param message the message content (possibly json)
     * @return the first response from the relay
     */
    default Mono<Response> publishPlainMono(String message) {
        return publishPlain(message).next();
    }
}
