package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Instant;
import java.util.Optional;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/42.md">NIP-42</a>.
 */
public final class Nip42 {
    private static final Kind AUTH_EVENT_KIND = Kind.of(22_242);
    private static final String CHALLENGE_TAG_NAME = "challenge";
    private static final String RELAY_TAG_NAME = "relay";

    public static TagValue challenge(String challenge) {
        return MoreTags.named(CHALLENGE_TAG_NAME, challenge);
    }

    public static TagValue relay(RelayUri relayUri) {
        return MoreTags.named(RELAY_TAG_NAME, relayUri.getUri().toString());
    }

    public static Optional<String> getChallenge(Event event) {
        return MoreTags.findByNameSingle(event, CHALLENGE_TAG_NAME)
                .filter(it -> it.getValuesCount() > 0)
                .map(it -> it.getValues(0));
    }

    public static Optional<RelayUri> getRelay(Event event) {
        return MoreTags.findByNameSingle(event, RELAY_TAG_NAME)
                .filter(it -> it.getValuesCount() > 0)
                .map(it -> RelayUri.of(it.getValues(0)));
    }

    public static Event.Builder createAuthEvent(XonlyPublicKey publicKey,
                                                String challenge,
                                                RelayUri relayUri) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(AUTH_EVENT_KIND.getValue())
                .addTags(challenge(challenge))
                .addTags(relay(relayUri))
                .setContent(""));
    }
}
