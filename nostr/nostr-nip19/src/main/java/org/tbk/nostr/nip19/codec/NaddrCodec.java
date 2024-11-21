package org.tbk.nostr.nip19.codec;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nip19.EntityType;
import org.tbk.nostr.nip19.Naddr;
import org.tbk.nostr.nip19.codec.util.Ints;
import org.tbk.nostr.nip19.codec.util.Tlv;
import org.tbk.nostr.nip19.codec.util.TlvType;
import org.tbk.nostr.util.MorePublicKeys;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class NaddrCodec implements Codec<Naddr> {
    @Override
    public boolean supports(String hrp, Class<?> clazz) {
        return EntityType.NADDR.getHrp().equals(hrp) && clazz.isAssignableFrom(Naddr.class);
    }

    @Override
    public Naddr decode(String hrp, byte[] data) {
        List<Tlv.Entry> entries = Tlv.decode(data);

        Tlv.Entry specialEntry = entries.stream()
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

        Tlv.Entry authorEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.AUTHOR.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.AUTHOR.getValue())));

        XonlyPublicKey author = Optional.of(authorEntry.getValue())
                .map(MorePublicKeys::fromBytes)
                .filter(it -> it.getPublicKey().isValid())
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: Invalid value with type %d.".formatted(TlvType.AUTHOR.getValue())));

        Tlv.Entry kindEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.KIND.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.KIND.getValue())));

        Kind kind = Optional.of(kindEntry.getValue())
                .map(Ints::fromByteArray)
                .filter(Kind::isValidKind)
                .map(Kind::of)
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: Invalid value with type %d.".formatted(TlvType.KIND.getValue())));

        return Naddr.builder()
                .eventUri(EventUri.of(kind, author.value.toHex(), dTagValue))
                .relays(relayEntries)
                .build();
    }

    @Override
    public byte[] encode(String hrp, Object data) {
        if (!supports(hrp, data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        Naddr value = ((Naddr) data);

        List<Tlv.Entry> entries = new LinkedList<>();
        entries.add(Tlv.Entry.builder()
                .type(TlvType.SPECIAL.getValue())
                .value(value.getEventUri().getIdentifier().orElse("").getBytes(StandardCharsets.UTF_8))
                .build());

        value.getRelays().forEach(it -> {
            entries.add(Tlv.Entry.builder()
                    .type(TlvType.RELAY.getValue())
                    .value(it.getUri().toASCIIString().getBytes())
                    .build());
        });
        entries.add(Tlv.Entry.builder()
                .type(TlvType.AUTHOR.getValue())
                .value(value.getEventUri().getPublicKey())
                .build());

        entries.add(Tlv.Entry.builder()
                .type(TlvType.KIND.getValue())
                .value(Ints.toByteArray(value.getEventUri().getKind().getValue()))
                .build());

        return Tlv.encode(entries);
    }
}
