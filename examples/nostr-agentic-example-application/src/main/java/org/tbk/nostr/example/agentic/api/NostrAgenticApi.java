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
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tbk.nostr.example.agentic.api.NostrAgenticApi.ListIdentitiesApiResponseDto.IdentityEntry;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.nip19.Nip19;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/agentic", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tags({
        @Tag(name = "agentic")
})
public class NostrAgenticApi {

    @NotNull
    private final OllamaApi ollamaApi;

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

    @Operation(
            summary = "List models that are available locally on the machine where Ollama is running."
    )
    @GetMapping(value = "/listmodels")
    public ResponseEntity<OllamaApi.ListModelResponse> listModels() {
        OllamaApi.ListModelResponse listModelResponse = ollamaApi.listModels();
        return ResponseEntity.ok(listModelResponse);
    }

    @Value
    @Builder
    @Jacksonized
    public static class EventPlainApiRequestDto {
        String contents;
    }

    @Operation(
            summary = "Generate a nostr event."
    )
    @PostMapping(value = "/nostr/event/plain")
    public ResponseEntity<String> eventPlain(@Validated @RequestBody EventPlainApiRequestDto body) {
        OllamaOptions options = OllamaOptions.builder()
                .temperature(0.33)
                .build();
        Prompt prompt = new Prompt(body.getContents(), options);
        ChatResponse response = ollamaChatModel.call(prompt);

        String text = response.getResult().getOutput().getText();

        Event event = MoreEvents.finalize(nostrSigner, Nip1.createTextNote(nostrSigner.getPublicKey(), text));

        return ResponseEntity.ok(JsonWriter.toJson(event));
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

    @Value
    @Builder
    public static class EventApiResponseDto {
        String json;
    }

    @Operation(
            summary = "Generate a nostr event."
    )
    @PostMapping(value = "/nostr/event")
    public ResponseEntity<EventApiResponseDto> event(@Validated @RequestBody EventApiRequestDto body) {
        Prompt prompt = new Prompt(body.getContents(), body.getOptions());
        ChatResponse response = ollamaChatModel.call(prompt);

        String text = response.getResult().getOutput().getText();

        Event event = MoreEvents.finalize(nostrSigner, Nip1.createTextNote(nostrSigner.getPublicKey(), text));

        return ResponseEntity.ok(EventApiResponseDto.builder()
                .json(JsonWriter.toJson(event))
                .build());
    }
}
