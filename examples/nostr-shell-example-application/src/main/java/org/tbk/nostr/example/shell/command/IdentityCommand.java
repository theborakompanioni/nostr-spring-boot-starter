package org.tbk.nostr.example.shell.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.stereotype.Component;
import org.tbk.nostr.example.shell.util.Json;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.nip19.Nip19;

import java.io.IOException;

@Slf4j
@Component
@CommandGroup(name = "Commands")
@RequiredArgsConstructor
class IdentityCommand {

    @Command(name = "identity", description = "Generate a nostr key pair")
    public String run() throws IOException {
        Identity.Account account = MoreIdentities.random().deriveAccount(0L);

        return Json.jsonPretty.composeString()
                .startObject()
                .put("privateKey", account.getPrivateKey().toHex())
                .put("publicKey", account.getPublicKey().value.toHex())
                .put("nsec", Nip19.encodeNsec(account.getPrivateKey()))
                .put("npub", Nip19.encodeNpub(account.getPublicKey()))
                .end()
                .finish();
    }
}
