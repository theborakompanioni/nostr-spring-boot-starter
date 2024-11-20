package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.util.MorePublicKeys;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

class NaddrCodec implements Codec<Naddr> {
    @Override
    public boolean supports(String hrp, Class<?> clazz) {
        return EntityType.NADDR.getHrp().equals(hrp) && clazz.isAssignableFrom(Naddr.class);
    }

    @Override
    public Naddr decode(String hrp, byte[] data) {
        List<TLV.Entry> entries = TLV.decode(data);

        TLV.Entry specialEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.SPECIAL.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.SPECIAL.getValue())));

        String dTagValue = new String(specialEntry.getValue(), StandardCharsets.UTF_8);

        List<RelayUri> relayEntries = entries.stream()
                .filter(it -> it.getType() == TlvType.RELAY.getValue())
                .map(it -> new String(it.getValue(), StandardCharsets.US_ASCII))
                .map(RelayUri::tryFromString)
                .flatMap(Optional::stream)
                .toList();

        TLV.Entry authorEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.AUTHOR.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.AUTHOR.getValue())));

        XonlyPublicKey author = Optional.of(authorEntry.getValue())
                .map(MorePublicKeys::fromBytes)
                .filter(it -> it.getPublicKey().isValid())
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: Invalid value with type %d.".formatted(TlvType.AUTHOR.getValue())));

        TLV.Entry kindEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.KIND.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.KIND.getValue())));

        Kind kind = Optional.of(kindEntry.getValue())
                .map(Ints::fromByteArray)
                .filter(Kind::isValidKind)
                .map(Kind::of)
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: Invalid value with type %d.".formatted(TlvType.KIND.getValue())));

        return Naddr.builder()
                .uri(EventUri.of(kind, author.value.toHex(), dTagValue))
                .relays(relayEntries)
                .build();
    }

    @Override
    public byte[] encode(String hrp, Object data) {
        throw new UnsupportedOperationException();
    }
}
