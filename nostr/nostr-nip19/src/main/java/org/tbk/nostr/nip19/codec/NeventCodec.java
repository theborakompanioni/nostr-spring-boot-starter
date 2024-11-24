package org.tbk.nostr.nip19.codec;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nip19.Nevent;
import org.tbk.nostr.nip19.Nip19Entity;
import org.tbk.nostr.nip19.codec.util.Ints;
import org.tbk.nostr.nip19.codec.util.Tlv;
import org.tbk.nostr.nip19.codec.util.TlvType;
import org.tbk.nostr.util.MorePublicKeys;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class NeventCodec implements Codec<Nevent> {
    @Override
    public boolean supports(Class<? extends Nip19Entity> clazz) {
        return clazz.isAssignableFrom(Nevent.class);
    }

    @Override
    public Nevent decode(byte[] data) {
        List<Tlv.Entry> entries = Tlv.decode(data);

        Tlv.Entry specialEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.SPECIAL.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.SPECIAL.getValue())));

        EventId eventId = EventId.of(specialEntry.getValue());

        List<RelayUri> relayEntries = entries.stream()
                .filter(it -> it.getType() == TlvType.RELAY.getValue())
                .map(it -> new String(it.getValue(), StandardCharsets.US_ASCII))
                .map(RelayUri::tryParse)
                .flatMap(Optional::stream)
                .toList();

        Optional<Tlv.Entry> authorEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.AUTHOR.getValue())
                .findFirst();

        Optional<XonlyPublicKey> author = authorEntry
                .map(Tlv.Entry::getValue)
                .map(MorePublicKeys::fromBytes)
                .filter(it -> it.getPublicKey().isValid());

        Optional<Tlv.Entry> kindEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.KIND.getValue())
                .findFirst();

        Optional<Kind> kind = kindEntry
                .map(Tlv.Entry::getValue)
                .map(Ints::fromByteArray)
                .filter(Kind::isValidKind)
                .map(Kind::of);

        return Nevent.builder()
                .eventId(eventId)
                .relays(relayEntries)
                .publicKey(author.orElse(null))
                .kind(kind.orElse(null))
                .build();
    }

    @Override
    public byte[] encode(Nip19Entity data) {
        if (!supports(data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        Nevent value = ((Nevent) data);

        List<Tlv.Entry> entries = new LinkedList<>();
        entries.add(Tlv.Entry.builder().type(TlvType.SPECIAL.getValue()).value(value.getEventId().toByteArray()).build());
        value.getRelays().forEach(it -> {
            entries.add(Tlv.Entry.builder().type(TlvType.RELAY.getValue()).value(it.getUri().toASCIIString().getBytes()).build());
        });
        value.getPublicKey().ifPresent(it -> {
            entries.add(Tlv.Entry.builder().type(TlvType.AUTHOR.getValue()).value(it.value.toByteArray()).build());
        });
        value.getKind().ifPresent(it -> {
            entries.add(Tlv.Entry.builder().type(TlvType.KIND.getValue()).value(Ints.toByteArray(it.getValue())).build());
        });

        return Tlv.encode(entries);
    }
}
