package org.tbk.nostr.example.shell.command;

import fr.acinq.bitcoin.PrivateKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.tbk.nostr.example.shell.util.Json;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.nips.Nip19;

import java.io.IOException;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class IdentityCommand {

    @ShellMethod(key = "identity", value = "Generate a nostr key pair")
    public String run() throws IOException {
        PrivateKey privateKey = MoreIdentities.random().deriveAccount(0L).getPrivateKey();

        return Json.jsonPretty.composeString()
                .startObject()
                .put("privateKey", privateKey.toHex())
                .put("publicKey", privateKey.xOnlyPublicKey().getPublicKey().toHex())
                .put("nsec", Nip19.toNsec(privateKey))
                .put("npub", Nip19.toNpub(privateKey.xOnlyPublicKey()))
                .end()
                .finish();
    }
}