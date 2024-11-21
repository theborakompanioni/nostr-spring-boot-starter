package org.tbk.nostr.example.shell.command;

import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.MnemonicCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.nostr.example.shell.util.Json;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.nip19.Nip19;

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
            @ShellOption(value = "name", help = "persona name (used to derive master key)") String name,
            @ShellOption(value = "account", defaultValue = "0", help = "identity account index (default: 0)") long accountIndexArg
    ) throws IOException {
        long accountIndex = accountIndexArg >= 0 ? accountIndexArg : 0;

        byte[] entropy = Arrays.copyOfRange(Crypto.sha256(name.getBytes(StandardCharsets.UTF_8)), 0, 16);

        List<String> mnemonics = MnemonicCode.toMnemonics(entropy);

        Identity identity = MoreIdentities.fromMnemonic(mnemonics);
        Identity.Account account = identity.deriveAccount(accountIndex);

        return Json.jsonPretty.composeString()
                .startObject()
                .put("entropy", HexFormat.of().formatHex(entropy))
                .put("mnemonic", String.join(" ", mnemonics))
                .put("keyPath", account.getPath().asString('\''))
                .put("privateKey", account.getPrivateKey().toHex())
                .put("publicKey", account.getPublicKey().value.toHex())
                .put("nsec", Nip19.encodeNsec(account.getPrivateKey()))
                .put("npub", Nip19.encodeNpub(account.getPublicKey()))
                .end()
                .finish();
    }
}