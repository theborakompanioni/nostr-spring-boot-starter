package org.tbk.nostr.util;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.tbk.nostr.util.MorePublicKeys.isValidPublicKey;

class MorePublicKeysTest {
    private static final Signer testSigner = SimpleSigner.fromPrivateKeyHex("01".repeat(32));

    @Test
    void itShouldValidatePublicKeys0Invalid() {
        assertThat(isValidPublicKey(new byte[0]), is(false));
        assertThat(isValidPublicKey(HexFormat.of().parseHex("00")), is(false));
        assertThat(isValidPublicKey(HexFormat.of().parseHex("00".repeat(16))), is(false));
        assertThat(isValidPublicKey(HexFormat.of().parseHex("00".repeat(32))), is(false));
        assertThat(isValidPublicKey(HexFormat.of().parseHex("11".repeat(32))), is(false));
    }

    /**
     * See <a href="https://github.com/nostr-protocol/nips/blob/master/06.md">NIP-6</a>.
     */
    @Test
    void itShouldValidatePublicKeys0Valid() {
        assertThat(isValidPublicKey(testSigner.getPublicKey().value.toByteArray()), is(true));
        assertThat(isValidPublicKey(SimpleSigner.random().getPublicKey().value.toByteArray()), is(true));
        assertThat(isValidPublicKey(HexFormat.of().parseHex("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca")), is(true));
        assertThat(isValidPublicKey(HexFormat.of().parseHex("17162c921dc4d2518f9a101db33695df1afb56ab82f5ff3e5da6eec3ca5cd917")), is(true));
        assertThat(isValidPublicKey(HexFormat.of().parseHex("d41b22899549e1f3d335a31002cfd382174006e166d3e658e3a5eecdb6463573")), is(true));
    }
}