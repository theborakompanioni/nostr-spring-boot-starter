package org.tbk.nostr.relay.nip42;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.NostrWebSocketSession;
import reactor.core.publisher.Mono;

import java.io.Serial;
import java.security.Principal;
import java.util.Collection;


public interface Nip42Support {

    String createNewChallenge(NostrWebSocketSession session);

    Mono<Boolean> requiresAuthentication(NostrRequestContext context, Request request);

    Mono<NostrAuthentication> attemptAuthentication(NostrRequestContext context, Event authEvent);

    interface NostrAuthentication extends Authentication {
        PublicKeyPrincipal getPrincipal();
    }

    interface PublicKeyPrincipal extends Principal {
        default String getName() {
            return getPublicKey().value.toHex();
        }

        XonlyPublicKey getPublicKey();
    }
}
