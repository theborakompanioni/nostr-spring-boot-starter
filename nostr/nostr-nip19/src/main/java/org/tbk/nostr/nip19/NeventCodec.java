package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.util.MorePublicKeys;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

class NeventCodec implements Codec<Nevent> {
    @Override
    public boolean supports(String hrp, Class<?> clazz) {
        return EntityType.NEVENT.getHrp().equals(hrp) && clazz.isAssignableFrom(Nevent.class);
    }

    @Override
    public Nevent decode(String hrp, byte[] data) {
        List<TLV.Entry> entries = TLV.decode(data);

        TLV.Entry specialEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.SPECIAL.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.SPECIAL.getValue())));

        EventId eventId = EventId.of(specialEntry.getValue());

        List<RelayUri> relayEntries = entries.stream()
                .filter(it -> it.getType() == TlvType.RELAY.getValue())
                .map(it -> new String(it.getValue(), StandardCharsets.US_ASCII))
                .map(RelayUri::tryFromString)
                .flatMap(Optional::stream)
                .toList();

        Optional<TLV.Entry> authorEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.AUTHOR.getValue())
                .findFirst();

        Optional<XonlyPublicKey> author = authorEntry
                .map(TLV.Entry::getValue)
                .map(MorePublicKeys::fromBytes)
                .filter(it -> it.getPublicKey().isValid());

        Optional<TLV.Entry> kindEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.KIND.getValue())
                .findFirst();

        Optional<Kind> kind = kindEntry
                .map(TLV.Entry::getValue)
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
}
