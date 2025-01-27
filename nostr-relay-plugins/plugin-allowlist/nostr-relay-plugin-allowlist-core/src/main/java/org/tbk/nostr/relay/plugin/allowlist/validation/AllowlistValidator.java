package org.tbk.nostr.relay.plugin.allowlist.validation;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.plugin.allowlist.Allowlist;
import org.tbk.nostr.relay.validation.EventValidator;
import org.tbk.nostr.util.MorePublicKeys;

@Slf4j
@RequiredArgsConstructor
public class AllowlistValidator implements EventValidator {

    @NonNull
    private final Allowlist allowlist;

    @Override
    public void validateEvent(Event target, Errors errors) {
        XonlyPublicKey pubkey = MorePublicKeys.fromEvent(target);

        if (!allowlist.isAllowed(pubkey)) {
            errors.rejectValue("pubkey", "event.pubkey.blocked", "blocked: pubkey is not allowed.");
        }
    }
}
