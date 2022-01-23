package org.tbk.nostr.identity;

import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.PrivateKey;

public final class MoreIdentities {

    private MoreIdentities() {
        throw new UnsupportedOperationException();
    }

    public static PrivateKey random() {
        return fromSeed(MoreRandom.randomByteArray(256));
    }

    public static PrivateKey fromSeed(byte[] seed) {
        DeterministicWallet.ExtendedPrivateKey extendedPrivateKey = DeterministicWallet.generate(seed);
        return extendedPrivateKey.getPrivateKey();
    }

    public static PrivateKey fromHex(String hex) {
        return PrivateKey.fromHex(hex);
    }

}
