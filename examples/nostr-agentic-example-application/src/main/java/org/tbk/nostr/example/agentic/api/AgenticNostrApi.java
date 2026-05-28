package org.tbk.nostr.example.agentic.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.nip19.Nip19;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.ProfileMetadata;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/nostr", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tags({
        @Tag(name = "nostr")
})
public class AgenticNostrApi {

    @NotNull
    private final OllamaChatModel ollamaChatModel;

    @NotNull
    private final Identity nostrIdentity;

    @NotNull
    private final Signer nostrSigner;

    @NotNull
    private final NostrTemplate nostrTemplate;

    @Operation(
            summary = "Currently active identity"
    )
    @GetMapping(value = "/whoami")
    public ResponseEntity<WhoAmIResponse> whoami() {
        return ResponseEntity.ok(WhoAmIResponse.builder()
                .identity(toIdentityEntry(nostrIdentity))
                .build());
    }

    @Operation(
            summary = "Profile of currently active identity"
    )
    @GetMapping(value = "/whoami/profile")
    public ResponseEntity<Event> whoamiProfile() {
        ProfileMetadata profileMetadata = nostrTemplate.fetchMetadataByAuthor(nostrSigner.getPublicKey())
                .blockOptional(Duration.ofSeconds(10))
                .orElse(null);

        // TODO: after nostr-proto supports serializing ProfileMetadata: don't wrap in an event
        return ResponseEntity.ok(Nip1.createMetadata(nostrSigner.getPublicKey(), profileMetadata).build());
    }
    private static IdentityEntry toIdentityEntry(Identity nostrIdentity) {
        return Optional.of(nostrIdentity)
                .map(it -> it.deriveAccount(0))
                .map(it -> IdentityEntry.builder()
                        .path(it.getPath().toString())
                        .publicKey(it.getPublicKey().value.toHex())
                        .npub(Nip19.encodeNpub(it))
                        .build())
                .orElseThrow();
    }

    @Operation(
            summary = "List available nostr identities."
    )
    @GetMapping(value = "/listidentities")
    public ResponseEntity<ListIdentitiesApiResponseDto> listIdentities() {
        return ResponseEntity.ok(ListIdentitiesApiResponseDto.builder()
                .addIdentity(toIdentityEntry(nostrIdentity))
                .build());
    }

    @Value
    @Builder
    public static class WhoAmIResponse {
        @NonNull
        IdentityEntry identity;
        @Nullable
        ProfileMetadata profileMetadata;
    }

    @Value
    @Builder
    public static class IdentityEntry {
        String path;
        @JsonProperty("public_key")
        String publicKey;
        String npub;
    }

    @Value
    @Builder
    public static class ListIdentitiesApiResponseDto {
        @Singular("addIdentity")
        List<IdentityEntry> identities;
    }

    @Value
    @Builder
    @Jacksonized
    public static class EventApiRequestDto {
        @NotNull
        @NotBlank
        @Size(max = 1024)
        String contents;

        @Builder.Default
        Double temperature = Double.valueOf("0.33");

        private OllamaChatOptions toOllamaChatOptions() {
            return OllamaChatOptions.builder()
                    .temperature(temperature)
                    .build();
        }
    }

    @Operation(
            summary = "Generate a nostr event."
    )
    @PostMapping(value = "/event")
    // Note: ResponseEntity<?> is used as workaround for swagger-ui loading issues with protobuf classes
    public ResponseEntity<?> event(@Validated @RequestBody EventApiRequestDto body) {
        Prompt prompt = new Prompt(body.getContents(), body.toOllamaChatOptions());
        ChatResponse response = ollamaChatModel.call(prompt);

        String text = response.getResult().getOutput().getText();

        Event event = MoreEvents.finalize(nostrSigner, Nip1.createTextNote(nostrSigner.getPublicKey(), text));

        return ResponseEntity.ok(event);
    }

    @Value
    @Builder
    public static class EventWithMetaApiResponseDto {
        Object event; // Note: Object is used as workaround for swagger-ui loading issues with protobuf classes

        Prompt prompt;

        @JsonProperty("chat_response")
        ChatResponse chatResponse;
    }

    @Operation(
            summary = "Generate a nostr event."
    )
    @PostMapping(value = "/event-with-meta")
    public ResponseEntity<EventWithMetaApiResponseDto> eventWithMeta(@Validated @RequestBody EventApiRequestDto body) {
        Prompt prompt = new Prompt(body.getContents(), body.toOllamaChatOptions());
        ChatResponse response = ollamaChatModel.call(prompt);

        String text = response.getResult().getOutput().getText();

        Event event = MoreEvents.finalize(nostrSigner, Nip1.createTextNote(nostrSigner.getPublicKey(), text));

        return ResponseEntity.ok(EventWithMetaApiResponseDto.builder()
                .event(event)
                .prompt(prompt)
                .chatResponse(response)
                .build());
    }
}
