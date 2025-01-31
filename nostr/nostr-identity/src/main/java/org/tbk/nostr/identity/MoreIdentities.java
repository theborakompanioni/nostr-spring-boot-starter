package org.tbk.nostr.identity;

import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.MnemonicCode;

import java.util.HexFormat;
import java.util.List;

public final class MoreIdentities {

    private MoreIdentities() {
        throw new UnsupportedOperationException();
    }

    public static Identity random() {
        return fromSeed(MoreRandom.randomByteArray(256));
    }

    public static Identity fromSeedHex(String hex) {
        return fromSeed(HexFormat.of().parseHex(hex));
    }

    public static Identity fromSeed(byte[] seed) {
        return new Identity(DeterministicWallet.generate(seed));
    }

    public static Identity.Account fromSeed(byte[] seed, long accountIndex) {
        return fromSeed(seed).deriveAccount(accountIndex);
    }

    public static Identity fromMnemonic(String mnemonic) {
        return fromMnemonic(mnemonic, "");
    }

    public static Identity fromMnemonic(String mnemonic, String passphrase) {
        return fromSeed(MnemonicCode.toSeed(mnemonic, passphrase));
    }

    public static Identity fromMnemonic(List<String> mnemonic) {
        return fromMnemonic(mnemonic, "");
    }

    public static Identity fromMnemonic(List<String> mnemonic, String passphrase) {
        return fromMnemonic(String.join(" ", mnemonic), passphrase);
    }
}
