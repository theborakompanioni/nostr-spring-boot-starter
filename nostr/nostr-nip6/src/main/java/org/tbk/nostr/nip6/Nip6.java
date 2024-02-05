package org.tbk.nostr.nip6;

import com.google.common.annotations.VisibleForTesting;
import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.KeyPath;
import fr.acinq.bitcoin.MnemonicCode;
import fr.acinq.bitcoin.PrivateKey;
import lombok.NonNull;
import reactor.core.publisher.Flux;

import java.util.HexFormat;

import static fr.acinq.bitcoin.DeterministicWallet.hardened;

public final class Nip6 {
    // m/44'/1237'/<account>'/0/0
    private static final KeyPath nip6Base = new KeyPath("")
            .derive(hardened(44L))
            .derive(hardened(1237L));

    private static KeyPath nip6Path(long account) {
        return nip6Base.derive(hardened(account))
                .derive(0L)
                .derive(0L);
    }

    private Nip6() {
        throw new UnsupportedOperationException();
    }

    public static PrivateKey fromSeed(byte[] seed) {
        return fromSeed(seed, 0);
    }

    public static PrivateKey fromSeed(byte[] seed, long account) {
        return fromMasterPrivateKey(DeterministicWallet.generate(seed), account);
    }

    public static PrivateKey fromSeed(byte[] seed, KeyPath keyPath) {
        return fromMasterPrivateKey(DeterministicWallet.generate(seed), keyPath);
    }

    public static Flux<PrivateKey> generateFromSeed(byte[] seed) {
        DeterministicWallet.ExtendedPrivateKey masterPrivateKey = DeterministicWallet.generate(seed);
        return Flux.generate(() -> 0L, (i, sink) -> {
            sink.next(fromMasterPrivateKey(masterPrivateKey, i));
            return i + 1;
        });
    }

    private static PrivateKey fromMasterPrivateKey(DeterministicWallet.ExtendedPrivateKey masterPrivateKey, long account) {
        return fromMasterPrivateKey(masterPrivateKey, nip6Path(account));
    }

    private static PrivateKey fromMasterPrivateKey(DeterministicWallet.ExtendedPrivateKey masterPrivateKey, KeyPath keyPath) {
        return DeterministicWallet.derivePrivateKey(masterPrivateKey, keyPath).getPrivateKey();
    }

    public static PrivateKey testVector0() {
        return Nip6.fromSeed(testVector0Seed());
    }

    public static PrivateKey testVector1() {
        return Nip6.fromSeed(testVector1Seed());
    }

    @VisibleForTesting
    static byte[] testVector0Seed() {
        return MnemonicCode.toSeed("leader monkey parrot ring guide accident before fence cannon height naive bean", "");
    }

    @VisibleForTesting
    static byte[] testVector1Seed() {
        return MnemonicCode.toSeed("what bleak badge arrange retreat wolf trade produce cricket blur garlic valid proud rude strong choose busy staff weather area salt hollow arm fade", "");
    }
}
