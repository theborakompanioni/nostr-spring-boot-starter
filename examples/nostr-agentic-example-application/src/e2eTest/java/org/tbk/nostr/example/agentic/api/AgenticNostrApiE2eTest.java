package org.tbk.nostr.example.agentic.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.tbk.nostr.example.agentic.utils.MostMinimalPrompt;
import tools.jackson.databind.json.JsonMapper;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class AgenticNostrApiE2eTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgenticModelApi sut;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void event() throws Exception {
        MostMinimalPrompt prompt = MostMinimalPrompt.create();
        AgenticNostrApi.EventApiRequestDto body = AgenticNostrApi.EventApiRequestDto.builder()
                .contents(prompt.getPrompt())
                .temperature(prompt.getOptions().getTemperature())
                .build();

        mockMvc.perform(post("/api/v1/nostr/event")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.pubkey", is(notNullValue())))
                .andExpect(jsonPath("$.created_at", is(notNullValue())))
                .andExpect(jsonPath("$.kind", is(notNullValue())))
                .andExpect(jsonPath("$.tags", is(notNullValue())))
                .andExpect(jsonPath("$.content", is(notNullValue())))
                .andExpect(jsonPath("$.sig", is(notNullValue())));
    }
}
