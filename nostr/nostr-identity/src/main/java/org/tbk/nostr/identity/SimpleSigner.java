package org.tbk.nostr.identity;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.*;
import org.tbk.nostr.proto.Event;

import java.util.HexFormat;

import static java.util.Objects.requireNonNull;

public class SimpleSigner implements Signer {

    public static SimpleSigner random() {
        return fromPrivateKey(MoreIdentities.random());
    }

    public static SimpleSigner fromHex(String hex) {
        return fromPrivateKey(MoreIdentities.fromHex(hex));
    }

    public static SimpleSigner fromPrivateKey(PrivateKey privateKey) {
        return new SimpleSigner(privateKey);
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
        if (builder.getId().isEmpty()) {
            throw new IllegalArgumentException("Missing id");
        }

        try {
            ByteVector32 data = ByteVector32.fromValidHex(HexFormat.of().formatHex(builder.getId().toByteArray()));
            ByteVector32 auxRand = ByteVector32.fromValidHex(HexFormat.of().formatHex(MoreRandom.randomByteArray(32)));
            ByteVector64 signature = Crypto.signSchnorr(data, this.privateKey, Crypto.SchnorrTweak.NoTweak.INSTANCE, auxRand);

            // sanity check
            boolean validSignature = Crypto.verifySignatureSchnorr(data, signature, privateKey.xOnlyPublicKey());
            if (!validSignature) {
                throw new IllegalStateException("Invalid signature");
            }

            return builder.setSig(ByteString.copyFrom(signature.toByteArray()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
