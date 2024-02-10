package org.tbk.nostr.nip11;

import com.fasterxml.jackson.jr.ob.JSON;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.util.MorePublicKeys;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RelayInfoDocumentTest {

    @Test
    void toJson0() throws IOException {
        RelayInfoDocument sut = RelayInfoDocument.newBuilder().build();

        assertThat(JSON.std.anyFrom(sut.toJson()), is(JSON.std.anyFrom("""
                {
                  "supported_nips": [ ]
                }
                """)));
        assertThat(sut.toJson(), is(RelayInfoDocument.toJson(sut)));
    }

    @Test
    void toJson1() throws IOException {
        RelayInfoDocument sut = RelayInfoDocument.newBuilder()
                .name("nostr.land")
                .description("nostr.land family of relays (us-or-01)")
                .pubkey(MorePublicKeys.fromHex("52b4a076bcbbbdc3a1aefa3735816cf74993b1b8db202b01c883c58be7fad8bd"))
                .software(URI.create("custom"))
                .supportedNips(List.of(1, 2, 4, 9, 11, 12, 16, 20, 22, 28, 33, 40))
                .version("1.0.1")
                .build();

        String expected = """
                {
                  "description": "nostr.land family of relays (us-or-01)",
                  "name": "nostr.land",
                  "pubkey": "52b4a076bcbbbdc3a1aefa3735816cf74993b1b8db202b01c883c58be7fad8bd",
                  "software": "custom",
                  "supported_nips": [
                    1,
                    2,
                    4,
                    9,
                    11,
                    12,
                    16,
                    20,
                    22,
                    28,
                    33,
                    40
                  ],
                  "version": "1.0.1"
                }
                """;
        assertThat(JSON.std.anyFrom(sut.toJson()), is(JSON.std.anyFrom(expected)));
        assertThat(sut.toJson(), is(RelayInfoDocument.toJson(sut)));
    }

    @Test
    void fromJson0() {
        RelayInfoDocument expected = RelayInfoDocument.newBuilder().build();

        assertThat(RelayInfoDocument.fromJson("{}"), is(expected));
        assertThat(RelayInfoDocument.fromJson("""
                {
                  "supported_nips": [ ]
                }
                """), is(expected));
    }

    @Test
    void fromJson1() {
        String json = """
                {
                  "description": "nostr.land family of relays (us-or-01)",
                  "name": "nostr.land",
                  "pubkey": "52b4a076bcbbbdc3a1aefa3735816cf74993b1b8db202b01c883c58be7fad8bd",
                  "software": "custom",
                  "supported_nips": [
                    1,
                    2,
                    4,
                    9,
                    11,
                    12,
                    16,
                    20,
                    22,
                    28,
                    33,
                    40
                  ],
                  "version": "1.0.1"
                }
                """;

        RelayInfoDocument expected = RelayInfoDocument.newBuilder()
                .name("nostr.land")
                .description("nostr.land family of relays (us-or-01)")
                .pubkey(MorePublicKeys.fromHex("52b4a076bcbbbdc3a1aefa3735816cf74993b1b8db202b01c883c58be7fad8bd"))
                .software(URI.create("custom"))
                .supportedNips(List.of(1, 2, 4, 9, 11, 12, 16, 20, 22, 28, 33, 40))
                .version("1.0.1")
                .build();

        assertThat(RelayInfoDocument.fromJson(json), is(expected));
    }
}
