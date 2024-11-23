package org.tbk.nostr.persona;

import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.MnemonicCode;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class Persona {
    private Persona() {
        throw new UnsupportedOperationException();
    }

    public static Identity alice() {
        return persona("alice");
    }

    public static Identity bob() {
        return persona("bob");
    }

    public static Identity persona(String name) {
        byte[] entropy = Arrays.copyOfRange(Crypto.sha256(name.getBytes(StandardCharsets.UTF_8)), 0, 16);
        List<String> mnemonics = MnemonicCode.toMnemonics(entropy);
        return MoreIdentities.fromMnemonic(mnemonics);
    }
}
