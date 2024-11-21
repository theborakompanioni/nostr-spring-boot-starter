package org.tbk.nostr.nip19.codec.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class TlvTest {

    private static String hex(byte... bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    private static byte[] fromHex(String hex) {
        return HexFormat.of().parseHex(hex);
    }

    @Test
    void convert0() {
        byte[] encoded = Tlv.encode(Collections.emptyList());
        assertThat(encoded.length, is(0));

        List<Tlv.Entry> decoded = Tlv.decode(encoded);
        assertThat(decoded, hasSize(0));
    }

    @Test
    void convert1() {
        byte[] encoded = Tlv.encode(List.of(
                Tlv.Entry.builder().type((byte) 0).value(new byte[]{}).build()
        ));
        assertThat(encoded.length, is(2));
        assertThat(hex(encoded), is("0000"));

        List<Tlv.Entry> decoded = Tlv.decode(encoded);
        assertThat(decoded, hasSize(1));
    }

    @Test
    void convert2() {
        byte[] encoded = Tlv.encode(List.of(
                Tlv.Entry.builder().type((byte) 0).value(new byte[]{}).build(),
                Tlv.Entry.builder().type((byte) 1).value(new byte[]{1}).build(),
                Tlv.Entry.builder().type((byte) 21).value(new byte[]{2}).build(),
                Tlv.Entry.builder().type((byte) 255).value(new byte[]{3, 127}).build(),
                Tlv.Entry.builder().type((byte) 21_000_000).value(new byte[]{4, -127}).build(),
                Tlv.Entry.builder().type((byte) Integer.MAX_VALUE).value(new byte[]{1, 11, 111}).build()
        ));
        assertThat(encoded.length, is(6 * 2 + 9));
        assertThat(HexFormat.of().formatHex(encoded), is("0000010101150102ff02037f40020481ff03010b6f"));

        List<Tlv.Entry> decoded = Tlv.decode(encoded);
        assertThat(decoded, hasSize(6));

        assertThat(hex(decoded.get(0).getType()), is("00"));
        assertThat(hex(decoded.get(0).getValue()), is(""));

        assertThat(hex(decoded.get(1).getType()), is("01"));
        assertThat(hex(decoded.get(1).getValue()), is("01"));

        assertThat(hex(decoded.get(2).getType()), is("15"));
        assertThat(hex(decoded.get(2).getValue()), is("02"));

        assertThat(hex(decoded.get(3).getType()), is("ff"));
        assertThat(hex(decoded.get(3).getValue()), is("037f"));

        assertThat(hex(decoded.get(4).getType()), is("40"));
        assertThat(hex(decoded.get(4).getValue()), is("0481"));

        assertThat(hex(decoded.get(5).getType()), is("ff"));
        assertThat(hex(decoded.get(5).getValue()), is("010b6f"));
    }

    @Test
    void decodeLengthZero() {
        assertThat(Tlv.decode(fromHex("")), hasSize(0));
        assertThat(Tlv.decode(fromHex("00")), hasSize(0));
        assertThat(Tlv.decode(fromHex("1111")), hasSize(0));
        assertThat(Tlv.decode(fromHex("0123456789")), hasSize(0));
        assertThat(Tlv.decode(fromHex("0001")), hasSize(0));
    }

    @Test
    void decodeLengthOne() {
        assertThat(Tlv.decode(fromHex("0000")), hasSize(1));
        assertThat(Tlv.decode(fromHex("110100")), hasSize(1));
        assertThat(Tlv.decode(fromHex("22020102")), hasSize(1));
        assertThat(Tlv.decode(fromHex("3303010203")), hasSize(1));
        assertThat(Tlv.decode(fromHex("440f000000000000000000000000000001")), hasSize(1));
        assertThat(Tlv.decode(fromHex("3303010203ff01")), hasSize(1));
    }

    @Test
    void decodeLengthTwo() {
        assertThat(Tlv.decode(fromHex("00000000")), hasSize(2));
        assertThat(Tlv.decode(fromHex("000100000100")), hasSize(2));
        assertThat(Tlv.decode(fromHex("11011111011ff0f56789")), hasSize(2));
        assertThat(Tlv.decode(fromHex("22030000002200")), hasSize(2));
    }
}