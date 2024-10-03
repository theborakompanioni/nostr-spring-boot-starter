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
        return fromSeed(seed, 0L);
    }
    public static PrivateKey fromSeed(byte[] seed, long accountIndex) {
        return Nip6.fromSeed(seed, accountIndex);
    }

    public static PrivateKey fromMnemonic(String mnemonic) {
        return fromMnemonic(mnemonic, "");
    }

    public static PrivateKey fromMnemonic(String mnemonic, String passphrase) {
        return fromMnemonic(mnemonic, passphrase, 0L);
    }

    public static PrivateKey fromMnemonic(String mnemonic, long accountIndex) {
        return fromMnemonic(mnemonic, "", accountIndex);
    }

    public static PrivateKey fromMnemonic(String mnemonic, String passphrase, long accountIndex) {
        return fromSeed(MnemonicCode.toSeed(mnemonic, passphrase), accountIndex);
    }

    public static PrivateKey fromMnemonic(List<String> mnemonic) {
        return fromMnemonic(mnemonic, "");
    }

    public static PrivateKey fromMnemonic(List<String> mnemonic, String passphrase) {
        return fromMnemonic(mnemonic, passphrase, 0L);
    }

    public static PrivateKey fromMnemonic(List<String> mnemonic, long accountIndex) {
        return fromMnemonic(mnemonic, "", accountIndex);
    }

    public static PrivateKey fromMnemonic(List<String> mnemonic, String passphrase, long accountIndex) {
        return fromMnemonic(String.join(" ", mnemonic), passphrase, accountIndex);
    }
}
