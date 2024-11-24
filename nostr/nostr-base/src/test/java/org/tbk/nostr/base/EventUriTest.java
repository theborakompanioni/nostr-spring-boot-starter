package org.tbk.nostr.base;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class EventUriTest {

    @Test
    void testEquals0() {
        assertThat(EventUri.parse("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"),
                is(EventUri.of(1, "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2")));

        assertThat(EventUri.parse("2:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:"),
                is(EventUri.of(2, "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2", "")));

        assertThat(EventUri.parse("3:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:test"),
                is(EventUri.of(3, "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2", "test")));

        assertThat(EventUri.parse("4:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2: : test :"),
                is(EventUri.of(4, "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2", " : test :")));
    }

    @Test
    void createFromString0() {
        assertThat(EventUri.parse("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"), is(notNullValue()));
        assertThat(EventUri.parse("2:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:"), is(notNullValue()));
        assertThat(EventUri.parse("3:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:test"), is(notNullValue()));
        assertThat(EventUri.parse("4:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2: : test :"), is(notNullValue()));
    }

    @Test
    void createFromString1() {
        EventUri uri = EventUri.parse("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2");
        assertThat(uri.getKind().getValue(), is(1));
        assertThat(uri.getPublicKeyHex(), is("82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"));
        assertThat(uri.getIdentifier().isPresent(), is(false));
    }

    @Test
    void createFromString2() {
        EventUri uri = EventUri.parse("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:");
        assertThat(uri.getKind().getValue(), is(1));
        assertThat(uri.getPublicKeyHex(), is("82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"));
        assertThat(uri.getIdentifier().orElseThrow(), is(""));
    }

    @Test
    void createFromString3() {
        EventUri uri = EventUri.parse("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:test");
        assertThat(uri.getKind().getValue(), is(1));
        assertThat(uri.getPublicKeyHex(), is("82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"));
        assertThat(uri.getIdentifier().orElseThrow(), is("test"));
    }

    @Test
    void createFromString4() {
        EventUri uri = EventUri.parse("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:::::: test :::::");
        assertThat(uri.getKind().getValue(), is(1));
        assertThat(uri.getPublicKeyHex(), is("82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"));
        assertThat(uri.getIdentifier().orElseThrow(), is("::::: test :::::"));
    }

    @Test
    void isValid() {
        assertThat(EventUri.isValidEventUriString("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"), is(true));
        assertThat(EventUri.isValidEventUriString("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:"), is(true));
        assertThat(EventUri.isValidEventUriString("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2:test"), is(true));
        assertThat(EventUri.isValidEventUriString("1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2: : test :"), is(true));

        assertThat(EventUri.isValidEventUriString("-1:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"), is(false));
        assertThat(EventUri.isValidEventUriString("21000000:82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2"), is(false));
        assertThat(EventUri.isValidEventUriString("1:82341f882b6eabcd2ba7f1ef90aa"), is(false));
        assertThat(EventUri.isValidEventUriString("1:%s".formatted("x".repeat(64))), is(false));
    }
}
