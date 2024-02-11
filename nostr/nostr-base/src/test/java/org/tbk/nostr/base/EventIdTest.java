package org.tbk.nostr.base;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class EventIdTest {
    private static final EventId id0 = EventId.fromHex("87a351b42ddd95e4954f58a129286b94dc9d467ef76ebdaf6dec968d5ae639de");
    private static final EventId id1 = EventId.fromHex("7f0fdf9021cbde815007340d603b43e61ddecac58a337165974b74eb843ba4bc");
    private static final EventId id2 = EventId.fromHex("000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358");
    private static final EventId id3 = EventId.fromHex("00000000000009fb3d037ce2fcb5bd2a84914f98e6f13352495bbc5b708d162c");
    private static final EventId id4 = EventId.fromHex("0000000000000000000000000000000c25e1493f5a8465fd71802790e2a58a32");
    private static final EventId id5 = EventId.fromHex("00000000000000000000000000000000000000000000002a6fbf90a18e816d60");

    @Test
    void testToString() {
        assertThat(id0.toString(), is("EventId[87a351b42ddd95e4954f58a129286b94dc9d467ef76ebdaf6dec968d5ae639de]"));
    }

    @Test
    void testEquals() {
        assertThat(id0, is(equalTo(id0)));
        assertThat(id0.equals(id0), is(true));
        assertThat(id0.equals(EventId.fromHex("87a351b42ddd95e4954f58a129286b94dc9d467ef76ebdaf6dec968d5ae639de")), is(true));
        assertThat(id0, is(not(equalTo(id1))));
        assertThat(id0.equals(id1), is(false));
    }

    @Test
    void testCompareTo() {
        assertThat(EventId.fromHex("87a351b42ddd95e4954f58a129286b94dc9d467ef76ebdaf6dec968d5ae639de"),
                is(greaterThan(EventId.fromHex("00001ceb2fa31af298874dd04b8c42cfe0cb98af9e904d8639c35a8d5f94b5a6"))));

        assertThat(id0, is(greaterThanOrEqualTo(id0)));
        assertThat(id0, is(lessThanOrEqualTo(id0)));
        assertThat(id0.compareTo(id0), is(0));
        assertThat(id0.compareTo(EventId.fromHex("87a351b42ddd95e4954f58a129286b94dc9d467ef76ebdaf6dec968d5ae639de")), is(0));

        assertThat(id0.compareTo(id1), is(1));
        assertThat(id0.compareTo(id2), is(1));
        assertThat(id0.compareTo(id3), is(1));
        assertThat(id0.compareTo(id4), is(1));
        assertThat(id0.compareTo(id5), is(1));

        assertThat(id1.compareTo(id0), is(-1));
        assertThat(id1.compareTo(id2), is(1));
        assertThat(id1.compareTo(id3), is(1));
        assertThat(id1.compareTo(id4), is(1));
        assertThat(id1.compareTo(id5), is(1));

        assertThat(id2.compareTo(id0), is(-1));
        assertThat(id2.compareTo(id1), is(-1));
        assertThat(id2.compareTo(id3), is(1));
        assertThat(id2.compareTo(id4), is(1));
        assertThat(id2.compareTo(id5), is(1));

        assertThat(id3.compareTo(id0), is(-1));
        assertThat(id3.compareTo(id1), is(-1));
        assertThat(id3.compareTo(id2), is(-1));
        assertThat(id3.compareTo(id4), is(1));
        assertThat(id3.compareTo(id5), is(1));

        assertThat(id4.compareTo(id0), is(-1));
        assertThat(id4.compareTo(id1), is(-1));
        assertThat(id4.compareTo(id2), is(-1));
        assertThat(id4.compareTo(id3), is(-1));
        assertThat(id4.compareTo(id5), is(1));

        assertThat(id5.compareTo(id0), is(-1));
        assertThat(id5.compareTo(id1), is(-1));
        assertThat(id5.compareTo(id2), is(-1));
        assertThat(id5.compareTo(id3), is(-1));
        assertThat(id5.compareTo(id4), is(-1));
    }
}