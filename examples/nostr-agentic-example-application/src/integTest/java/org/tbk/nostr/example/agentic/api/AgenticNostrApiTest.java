package org.tbk.nostr.example.agentic.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG, printOnlyOnFailure = false)
@ActiveProfiles("test")
class AgenticNostrApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgenticModelApi sut;

    @Test
    void listIdentities() throws Exception {
        mockMvc.perform(get("/api/v1/nostr/listidentities")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identities[0].path", is("m/44'/1237'/0'/0/0")))
                .andExpect(jsonPath("$.identities[0].public_key", is(notNullValue())))
                .andExpect(jsonPath("$.identities[0].npub", startsWith("npub1")));
    }
}
