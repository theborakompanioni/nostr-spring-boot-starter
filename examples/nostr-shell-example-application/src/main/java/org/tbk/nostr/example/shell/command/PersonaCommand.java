package org.tbk.nostr.example.shell.command;

import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.MnemonicCode;
import fr.acinq.bitcoin.PrivateKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.nostr.example.shell.util.Json;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.nips.Nip19;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class PersonaCommand {

    @ShellMethod(key = "persona", value = "Generate nostr personas")
    public String run(
            @ShellOption(value = "name", help = "persona name (used to derive master key)") String name
    ) throws IOException {
        byte[] entropy = Arrays.copyOfRange(Crypto.sha256(name.getBytes(StandardCharsets.UTF_8)), 0, 16);

        List<String> mnemonics = MnemonicCode.toMnemonics(entropy);

        PrivateKey privateKey = MoreIdentities.fromMnemonic(mnemonics);

        return Json.jsonPretty.composeString()
                .startObject()
                .put("entropy", HexFormat.of().formatHex(entropy))
                .put("mnemonic", String.join(" ", mnemonics))
                .put("privateKey", privateKey.toHex())
                .put("publicKey", privateKey.xOnlyPublicKey().value.toHex())
                .put("nsec", Nip19.toNsec(privateKey))
                .put("npub", Nip19.toNpub(privateKey.xOnlyPublicKey()))
                .end()
                .finish();
    }
}