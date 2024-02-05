package org.tbk.nostr.nip6;

import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.KeyPath;
import fr.acinq.bitcoin.PrivateKey;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static fr.acinq.bitcoin.DeterministicWallet.hardened;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/06.md">NIP-6</a>.
 */
class Nip6Test {
    private static final byte[] nip6TestSeed0 = Nip6.testVector0Seed();
    private static final byte[] nip6TestSeed1 = Nip6.testVector1Seed();

    /**
     * mnemonic: leader monkey parrot ring guide accident before fence cannon height naive bean
     * private key (hex): 7f7ff03d123792d6ac594bfa67bf6d0c0ab55b6b1fdb6249303fe861f1ccba9a
     * nsec: nsec10allq0gjx7fddtzef0ax00mdps9t2kmtrldkyjfs8l5xruwvh2dq0lhhkp
     * public key (hex): 17162c921dc4d2518f9a101db33695df1afb56ab82f5ff3e5da6eec3ca5cd917
     * npub: npub1zutzeysacnf9rru6zqwmxd54mud0k44tst6l70ja5mhv8jjumytsd2x7nu
     */
    @Test
    void fromSeedNip6TestVector0() {
        PrivateKey privateKey0 = Nip6.fromSeed(nip6TestSeed0);
        assertThat(privateKey0, is(Nip6.testVector0()));
        assertThat(privateKey0.toHex(), is("7f7ff03d123792d6ac594bfa67bf6d0c0ab55b6b1fdb6249303fe861f1ccba9a"));

        PrivateKey privateKey21 = Nip6.fromSeed(nip6TestSeed0, 21);
        assertThat(privateKey21.toHex(), is("576390ec69951fcfbf159f2aac0965bb2e6d7a07da2334992af3225c57eaefca"));
    }

    /**
     * mnemonic: what bleak badge arrange retreat wolf trade produce cricket blur garlic valid proud rude strong choose busy staff weather area salt hollow arm fade
     * private key (hex): c15d739894c81a2fcfd3a2df85a0d2c0dbc47a280d092799f144d73d7ae78add
     * nsec: nsec1c9wh8xy5eqdzln7n5t0ctgxjcrdug73gp5yj0x03gntn67h83twssdfhel
     * public key (hex): d41b22899549e1f3d335a31002cfd382174006e166d3e658e3a5eecdb6463573
     * npub: npub16sdj9zv4f8sl85e45vgq9n7nsgt5qphpvmf7vk8r5hhvmdjxx4es8rq74h
     */
    @Test
    void fromSeedNip6TestVector1() {
        PrivateKey privateKey0 = Nip6.fromSeed(nip6TestSeed1);
        assertThat(privateKey0, is(Nip6.testVector1()));
        assertThat(privateKey0.toHex(), is("c15d739894c81a2fcfd3a2df85a0d2c0dbc47a280d092799f144d73d7ae78add"));

        PrivateKey privateKey42 = Nip6.fromSeed(nip6TestSeed1, 42);
        assertThat(privateKey42.toHex(), is("ad993054383da74e955f8b86346365b5ffd6575992e1de3738dda9f94407052b"));
    }

    @Test
    void generateFromSeedNip6TestVector0() {
        PrivateKey privateKey0 = Nip6.generateFromSeed(nip6TestSeed0).next().blockOptional().orElseThrow();
        assertThat(privateKey0.toHex(), is("7f7ff03d123792d6ac594bfa67bf6d0c0ab55b6b1fdb6249303fe861f1ccba9a"));

        PrivateKey privateKey21 = Nip6.generateFromSeed(nip6TestSeed0).skip(21).next().blockOptional().orElseThrow();
        assertThat(privateKey21.toHex(), is("576390ec69951fcfbf159f2aac0965bb2e6d7a07da2334992af3225c57eaefca"));
    }

    @Test
    void generateFromSeedNip6TestVector1() {
        PrivateKey firstPrivateKey = Nip6.generateFromSeed(nip6TestSeed1).next().blockOptional().orElseThrow();
        assertThat(firstPrivateKey.toHex(), is("c15d739894c81a2fcfd3a2df85a0d2c0dbc47a280d092799f144d73d7ae78add"));

        PrivateKey privateKey42 = Nip6.generateFromSeed(nip6TestSeed1).skip(42).next().blockOptional().orElseThrow();
        assertThat(privateKey42.toHex(), is("ad993054383da74e955f8b86346365b5ffd6575992e1de3738dda9f94407052b"));
    }

    // https://bips.xyz/32#test-vector-1
    @Test
    void fromSeedBip32TestVector1() {
        byte[] seed = HexFormat.of().parseHex("000102030405060708090a0b0c0d0e0f");

        KeyPath keyPath0 = new KeyPath("");
        PrivateKey expected0 = DeterministicWallet.ExtendedPrivateKey.decode("xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi", keyPath0).getSecond().getPrivateKey();
        PrivateKey actual0 = Nip6.fromSeed(seed, keyPath0);
        assertThat(actual0, is(expected0));

        KeyPath keyPath5 = new KeyPath("").derive(hardened(0)).derive(1).derive(hardened(2)).derive(2).derive(1_000_000_000);
        PrivateKey expected5 = DeterministicWallet.ExtendedPrivateKey.decode("xprvA41z7zogVVwxVSgdKUHDy1SKmdb533PjDz7J6N6mV6uS3ze1ai8FHa8kmHScGpWmj4WggLyQjgPie1rFSruoUihUZREPSL39UNdE3BBDu76", keyPath0).getSecond().getPrivateKey();
        PrivateKey actual5 = Nip6.fromSeed(seed, keyPath5);
        assertThat(actual5, is(expected5));
    }

    // https://bips.xyz/32#test-vector-4
    @Test
    void fromSeedBip32TestVector4() {
        byte[] seed = HexFormat.of().parseHex("3ddd5602285899a946114506157c7997e5444528f3003f6134712147db19b678");

        KeyPath keyPath0 = new KeyPath("");
        PrivateKey expected0 = DeterministicWallet.ExtendedPrivateKey.decode("xprv9s21ZrQH143K48vGoLGRPxgo2JNkJ3J3fqkirQC2zVdk5Dgd5w14S7fRDyHH4dWNHUgkvsvNDCkvAwcSHNAQwhwgNMgZhLtQC63zxwhQmRv", keyPath0).getSecond().getPrivateKey();
        PrivateKey actual0 = Nip6.fromSeed(seed, keyPath0);
        assertThat(actual0, is(expected0));

        KeyPath keyPath2 = new KeyPath("").derive(hardened(0)).derive(hardened(1));
        PrivateKey expected2 = DeterministicWallet.ExtendedPrivateKey.decode("xprv9xJocDuwtYCMNAo3Zw76WENQeAS6WGXQ55RCy7tDJ8oALr4FWkuVoHJeHVAcAqiZLE7Je3vZJHxspZdFHfnBEjHqU5hG1Jaj32dVoS6XLT1", keyPath0).getSecond().getPrivateKey();
        PrivateKey actual2 = Nip6.fromSeed(seed, keyPath2);
        assertThat(actual2, is(expected2));
    }
}
