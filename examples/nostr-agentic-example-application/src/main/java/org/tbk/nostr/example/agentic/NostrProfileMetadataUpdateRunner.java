package org.tbk.nostr.example.agentic;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.ProfileMetadata;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
class NostrProfileMetadataUpdateRunner implements ApplicationRunner {
    @NonNull
    private final NostrTemplate template;
    @NonNull
    private final Signer signer;
    @NonNull
    private final ProfileMetadata profileMetadata;

    @Override
    public void run(ApplicationArguments args) {
        Optional<ProfileMetadata> existingProfileMetadata = this.template
                .fetchMetadataByAuthor(signer.getPublicKey())
                .blockOptional(Duration.ofSeconds(30));

        String oldProfileMetadataJsonOrEmpty = existingProfileMetadata.map(JsonWriter::toJson).orElse("");
        String newProfileMetadataJson = JsonWriter.toJson(profileMetadata);

        boolean shouldUpdateProfileMetadata = !oldProfileMetadataJsonOrEmpty.equals(newProfileMetadataJson);

        if (!shouldUpdateProfileMetadata) {
            log.info("Profile metadata is up to date: {}", oldProfileMetadataJsonOrEmpty);
        } else {
            Event profileMetadataEvent = MoreEvents.finalize(signer, Nip1.createMetadata(signer.getPublicKey(), profileMetadata));

            OkResponse ok = this.template.send(profileMetadataEvent)
                    .blockOptional(Duration.ofSeconds(30))
                    .orElse(null);

            if (ok == null) {
                log.warn("Did not get a ok response after updating profile metadata.");
            } else {
                if (ok.getSuccess()) {
                    log.info("Successfully updated profile metadata: {}", JsonWriter.toJson(profileMetadata));
                } else {
                    log.warn("Failed to update profile metadata: message:='{}'", ok.getMessage());
                }
            }
        }
    }
}
