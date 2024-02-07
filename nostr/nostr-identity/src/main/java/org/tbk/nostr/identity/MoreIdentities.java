package org.tbk.nostr.identity;

import fr.acinq.bitcoin.MnemonicCode;
import fr.acinq.bitcoin.PrivateKey;
import org.tbk.nostr.nip6.Nip6;

import java.util.HexFormat;
import java.util.List;

public final class MoreIdentities {

    private MoreIdentities() {
        throw new UnsupportedOperationException();
    }

    public static PrivateKey random() {
        return fromSeed(MoreRandom.randomByteArray(256));
    }

    public static PrivateKey fromHex(String hex) {
        return PrivateKey.fromHex(hex);
    }

    public static PrivateKey of(byte[] data) {
        return new PrivateKey(data);
    }

    public static PrivateKey fromSeedHex(String hex) {
        return fromSeed(HexFormat.of().parseHex(hex));
    }

    public static PrivateKey fromSeed(byte[] seed) {
        return Nip6.fromSeed(seed);
    }

    public static PrivateKey fromMnemonic(String mnemonic) {
        return fromMnemonic(mnemonic, "");
    }

    public static PrivateKey fromMnemonic(String mnemonic, String passphrase) {
        return fromSeed(MnemonicCode.toSeed(mnemonic, passphrase));
    }

    public static PrivateKey fromMnemonic(List<String> mnemonic) {
        return fromMnemonic(mnemonic, "");
    }

    public static PrivateKey fromMnemonic(List<String> mnemonic, String passphrase) {
        return fromSeed(MnemonicCode.toSeed(mnemonic, passphrase));
    }
}
