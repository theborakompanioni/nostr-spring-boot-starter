package org.tbk.nostr.example.agentic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/model", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tags({
        @Tag(name = "model")
})
public class AgenticModelApi {

    @NotNull
    private final OllamaApi ollamaApi;

    @Operation(
            summary = "List models that are available locally on the machine where Ollama is running."
    )
    @GetMapping(value = "/listmodels")
    public ResponseEntity<OllamaApi.ListModelResponse> listModels() {
        OllamaApi.ListModelResponse listModelResponse = ollamaApi.listModels();
        return ResponseEntity.ok(listModelResponse);
    }
}
