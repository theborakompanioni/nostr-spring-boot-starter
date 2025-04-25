package org.tbk.nostr.nip44;

import com.fasterxml.jackson.jr.ob.JSON;
import fr.acinq.bitcoin.PrivateKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.tbk.nostr.util.MorePublicKeys;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("unchecked")
class Nip44TestVectorsTest {

    private static final Map<String, Object> TEST_VECTORS = new HashMap<>();

    @BeforeAll
    public static void loadTestVectors() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("nip44.vectors.json");
        String json = classPathResource.getContentAsString(StandardCharsets.UTF_8);

        TEST_VECTORS.putAll(JSON.std.mapFrom(json));
    }

    @Test
    void itShouldReadTestVectorsCorrectly() {
        assertThat(TEST_VECTORS.size(), is(1));

        Map<String, Object> v2 = (Map<String, Object>) TEST_VECTORS.get("v2");
        assertThat(v2.size(), is(2));

        Map<String, Object> valid = (Map<String, Object>) v2.get("valid");
        assertThat(valid.size(), is(5));

        Map<String, Object> invalid = (Map<String, Object>) v2.get("invalid");
        assertThat(invalid.size(), is(3));
    }

    @Test
    void itShouldVerifyConversationKeyValid() {
        List<Map<String, String>> valid = (List<Map<String, String>>) ((Map<String, Object>) ((Map<String, Object>) TEST_VECTORS.get("v2")).get("valid")).get("get_conversation_key");
        assertThat(valid, hasSize(greaterThan(0)));

        for (Map<String, String> o : valid) {
            String sec1 = o.get("sec1");
            String pub2 = o.get("pub2");
            String conversationKey = o.get("conversation_key");

            PrivateKey conversationKey1 = Nip44.getConversationKey(
                    PrivateKey.fromHex(sec1),
                    MorePublicKeys.fromHex(pub2)
            );

            assertThat(conversationKey, is(conversationKey1.toHex()));
        }
    }

    @Test
    void itShouldVerifyConversationKeyInvalid() {
        List<Map<String, String>> invalid = (List<Map<String, String>>) ((Map<String, Object>) ((Map<String, Object>) TEST_VECTORS.get("v2")).get("invalid")).get("get_conversation_key");
        assertThat(invalid, hasSize(greaterThan(0)));

        for (Map<String, String> o : invalid) {
            String sec1 = o.get("sec1");
            String pub2 = o.get("pub2");

            Exception exception = Assertions.assertThrows(Exception.class, () -> {
                Nip44.getConversationKey(
                        PrivateKey.fromHex(sec1),
                        MorePublicKeys.fromHex(pub2)
                );
            });

            assertThat(exception.getMessage(), is("secp256k1_ec_pubkey_parse failed"));
        }
    }

    @Test
    void itShouldVerifyEncryption() throws Exception {
        List<Map<String, String>> valid = (List<Map<String, String>>) ((Map<String, Object>) ((Map<String, Object>) TEST_VECTORS.get("v2")).get("valid")).get("encrypt_decrypt");
        assertThat(valid, hasSize(greaterThan(0)));

        for (Map<String, String> o : valid) {
            String sec1 = o.get("sec1");
            String sec2 = o.get("sec2");
            String conversationKey = o.get("conversation_key");
            String nonce = o.get("nonce");
            String expectedPlaintext = o.get("plaintext");
            String expectedPayload = o.get("payload");

            PrivateKey conversationKey1 = Nip44.getConversationKey(
                    PrivateKey.fromHex(sec1),
                    PrivateKey.fromHex(sec2).xOnlyPublicKey()
            );
            assertThat(conversationKey, is(conversationKey1.toHex()));

            String payload = Nip44.encrypt(conversationKey1, expectedPlaintext, HexFormat.of().parseHex(nonce));
            assertThat(payload, is(expectedPayload));

            String plaintext = Nip44.decrypt(conversationKey1, expectedPayload);
            assertThat(plaintext, is(expectedPlaintext));
        }
    }

    @Test
    void itShouldVerifyMessageKeys() {
        Map<String, Object> valid = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) TEST_VECTORS.get("v2")).get("valid")).get("get_message_keys");

        PrivateKey conversationKey = PrivateKey.fromHex((String) valid.get("conversation_key"));
        List<Map<String, String>> keys = (List<Map<String, String>>) valid.get("keys");

        assertThat(keys, hasSize(greaterThan(0)));

        for (Map<String, String> o : keys) {
            String nonce = o.get("nonce");
            String chachaKey = o.get("chacha_key");
            String chachaNonce = o.get("chacha_nonce");
            String hmacKey = o.get("hmac_key");

            byte[][] messageKeys = Nip44.toMessageKeys(conversationKey, HexFormat.of().parseHex(nonce));

            assertThat(HexFormat.of().formatHex(messageKeys[0]), is(chachaKey));
            assertThat(HexFormat.of().formatHex(messageKeys[1]), is(chachaNonce));
            assertThat(HexFormat.of().formatHex(messageKeys[2]), is(hmacKey));
        }
    }

    @Test
    void itShouldVerifyDecryptionInvalid() {
        List<Map<String, String>> invalid = (List<Map<String, String>>) ((Map<String, Object>) ((Map<String, Object>) TEST_VECTORS.get("v2")).get("invalid")).get("decrypt");
        assertThat(invalid, hasSize(greaterThan(0)));

        for (Map<String, String> o : invalid) {
            PrivateKey conversationKey = PrivateKey.fromHex(o.get("conversation_key"));
            String payload = o.get("payload");

            Exception exception = Assertions.assertThrows(Exception.class, () -> {
                Nip44.decrypt(conversationKey, payload);
            });

            assertThat(exception, is(notNullValue()));
        }
    }

    @Test
    public void itShouldCalculatePaddedLength() {
        List<List<Integer>> valid = (List<List<Integer>>) ((Map<String, Object>) ((Map<String, Object>) TEST_VECTORS.get("v2")).get("valid")).get("calc_padded_len");
        assertThat(valid, hasSize(greaterThan(0)));

        for (List<Integer> o : valid) {
            int inputLength = o.get(0);
            int expectedPaddedLength = o.get(1);

            int paddedLength = Nip44.calcPaddedLength(inputLength);
            assertThat(paddedLength, is(expectedPaddedLength));
        }
    }
}
