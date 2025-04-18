package org.tbk.nostr.example.agentic.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tbk.nostr.example.agentic.api.AgenticNostrApi.ListIdentitiesApiResponseDto.IdentityEntry;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.nip19.Nip19;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.util.MoreEvents;

import javax.validation.constraints.NotNull;
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

    @Operation(
            summary = "List available nostr identities."
    )
    @GetMapping(value = "/listidentities")
    public ResponseEntity<ListIdentitiesApiResponseDto> listIdentities() {
        return ResponseEntity.ok(ListIdentitiesApiResponseDto.builder()
                .addIdentity(Optional.of(nostrIdentity)
                        .map(it -> it.deriveAccount(0))
                        .map(it -> IdentityEntry.builder()
                                .path(it.getPath().toString())
                                .publicKey(it.getPublicKey().value.toHex())
                                .npub(Nip19.encodeNpub(it))
                                .build()).orElseThrow())
                .build());
    }

    @Value
    @Builder
    public static class ListIdentitiesApiResponseDto {
        @Singular("addIdentity")
        List<IdentityEntry> identities;

        @Value
        @Builder
        public static class IdentityEntry {
            String path;
            @JsonProperty("public_key")
            String publicKey;
            String npub;
        }
    }

    @Value
    @Builder
    @Jacksonized
    public static class EventApiRequestDto {
        String contents;

        @Builder.Default
        OllamaOptions options = OllamaOptions.builder()
                .temperature(0.33)
                .build();
    }

    @Operation(
            summary = "Generate a nostr event."
    )
    @PostMapping(value = "/event")
    public ResponseEntity<Event> event(@Validated @RequestBody EventApiRequestDto body) {
        Prompt prompt = new Prompt(body.getContents(), body.getOptions());
        ChatResponse response = ollamaChatModel.call(prompt);

        String text = response.getResult().getOutput().getText();

        Event event = MoreEvents.finalize(nostrSigner, Nip1.createTextNote(nostrSigner.getPublicKey(), text));

        return ResponseEntity.ok(event);
    }

    @Value
    @Builder
    public static class EventWithMetaApiResponseDto {
        Event event;

        Prompt prompt;

        @JsonProperty("chat_response")
        ChatResponse chatResponse;
    }

    @Operation(
            summary = "Generate a nostr event."
    )
    @PostMapping(value = "/event-with-meta")
    public ResponseEntity<EventWithMetaApiResponseDto> eventWithMeta(@Validated @RequestBody EventApiRequestDto body) {
        Prompt prompt = new Prompt(body.getContents(), body.getOptions());
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
