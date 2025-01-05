package org.tbk.nostr.identity;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.*;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.proto.Event;

import java.util.HexFormat;

import static java.util.Objects.requireNonNull;

public class SimpleSigner implements Signer {

    public static SimpleSigner random() {
        return fromIdentity(MoreIdentities.random());
    }

    public static SimpleSigner fromPrivateKeyHex(String hex) {
        return fromPrivateKey(PrivateKey.fromHex(hex));
    }

    public static SimpleSigner fromPrivateKey(PrivateKey privateKey) {
        return new SimpleSigner(privateKey);
    }

    public static SimpleSigner fromIdentity(Identity identity) {
        return fromAccount(identity.deriveAccount(0L));
    }

    public static SimpleSigner fromAccount(Identity.Account account) {
        return fromPrivateKey(account.getPrivateKey());
    }

    private final PrivateKey privateKey;

    private SimpleSigner(PrivateKey privateKey) {
        if (!privateKey.isValid()) {
            throw new IllegalArgumentException("Invalid key");
        }
        this.privateKey = requireNonNull(privateKey);
    }

    @Override
    public XonlyPublicKey getPublicKey() {
        return this.privateKey.publicKey().xOnly();
    }

    @Override
    public Event.Builder sign(Event.Builder builder) {
        if (builder.getKind() == Kinds.kindDirectMessage.getValue()) {
            throw new IllegalArgumentException("Cannot sign event with kind %d".formatted(Kinds.kindDirectMessage.getValue()));
        }

        return signUnsafe(builder);
    }

    public Event.Builder signUnsafe(Event.Builder builder) {
        if (builder.getId().isEmpty()) {
            throw new IllegalArgumentException("Missing id");
        }

        try {
            ByteVector32 data = ByteVector32.fromValidHex(EventId.of(builder).toHex());
            ByteVector32 auxRand = ByteVector32.fromValidHex(HexFormat.of().formatHex(MoreRandom.randomByteArray(32)));
            ByteVector64 signature = Crypto.signSchnorr(data, this.privateKey, Crypto.SchnorrTweak.NoTweak.INSTANCE, auxRand);

            return builder.setSig(ByteString.copyFrom(signature.toByteArray()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
