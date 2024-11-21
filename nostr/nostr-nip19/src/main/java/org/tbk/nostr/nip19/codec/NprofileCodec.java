package org.tbk.nostr.nip19.codec;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nip19.EntityType;
import org.tbk.nostr.nip19.Nprofile;
import org.tbk.nostr.nip19.codec.util.Tlv;
import org.tbk.nostr.nip19.codec.util.TlvType;
import org.tbk.nostr.util.MorePublicKeys;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class NprofileCodec implements Codec<Nprofile> {
    @Override
    public boolean supports(String hrp, Class<?> clazz) {
        return EntityType.NPROFILE.getHrp().equals(hrp) && clazz.isAssignableFrom(Nprofile.class);
    }

    @Override
    public Nprofile decode(String hrp, byte[] data) {
        List<Tlv.Entry> entries = Tlv.decode(data);

        Tlv.Entry specialEntry = entries.stream()
                .filter(it -> it.getType() == TlvType.SPECIAL.getValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.SPECIAL.getValue())));

        XonlyPublicKey publicKey = Optional.of(specialEntry.getValue())
                .map(MorePublicKeys::fromBytes)
                .filter(it -> it.getPublicKey().isValid())
                .orElseThrow(() -> new IllegalArgumentException("Decoding failed: Invalid value with type %d.".formatted(TlvType.SPECIAL.getValue())));

        List<RelayUri> relayEntries = entries.stream()
                .filter(it -> it.getType() == TlvType.RELAY.getValue())
                .map(it -> new String(it.getValue(), StandardCharsets.US_ASCII))
                .map(RelayUri::tryFromString)
                .flatMap(Optional::stream)
                .toList();

        return Nprofile.builder()
                .publicKey(publicKey)
                .relays(relayEntries)
                .build();
    }

    @Override
    public byte[] encode(String hrp, Object data) {
        if (!supports(hrp, data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        Nprofile value = ((Nprofile) data);

        List<Tlv.Entry> entries = new LinkedList<>();
        entries.add(Tlv.Entry.builder().type(TlvType.SPECIAL.getValue()).value(value.getPublicKey().value.toByteArray()).build());
        value.getRelays().forEach(it -> {
            entries.add(Tlv.Entry.builder().type(TlvType.RELAY.getValue()).value(it.getUri().toASCIIString().getBytes()).build());
        });

        return Tlv.encode(entries);
    }
}
