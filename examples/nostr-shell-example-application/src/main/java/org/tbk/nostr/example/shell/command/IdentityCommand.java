package org.tbk.nostr.example.shell.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.tbk.nostr.example.shell.util.Json;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.nip19.Nip19;

import java.io.IOException;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class IdentityCommand {

    @ShellMethod(key = "identity", value = "Generate a nostr key pair")
    public String run() throws IOException {
        Identity.Account account = MoreIdentities.random().deriveAccount(0L);

        return Json.jsonPretty.composeString()
                .startObject()
                .put("privateKey", account.getPrivateKey().toHex())
                .put("publicKey", account.getPublicKey().value.toHex())
                .put("nsec", Nip19.toNsec(account.getPrivateKey()))
                .put("npub", Nip19.toNpub(account.getPublicKey()))
                .end()
                .finish();
    }
}