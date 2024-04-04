package org.tbk.nostr.example.relay.impl.nip50;

import com.github.pemistahl.lingua.api.Language;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Nip50SearchTermTest {

    @Test
    void tryParse0Simple() {
        Nip50SearchTerm val0 = Nip50SearchTerm.tryParse("purple").orElseThrow();
        assertThat(val0.getSearch(), is("purple"));
        assertThat(val0.getOptions(), is(Nip50SearchTerm.Options.defaultOptions()));

        Nip50SearchTerm val1 = Nip50SearchTerm.tryParse("purple OR orange").orElseThrow();
        assertThat(val1.getSearch(), is("purple OR orange"));
        assertThat(val1.getOptions(), is(Nip50SearchTerm.Options.defaultOptions()));

        Nip50SearchTerm val2 = Nip50SearchTerm.tryParse("language:en purple OR orange").orElseThrow();
        assertThat(val2.getSearch(), is("purple OR orange"));
        assertThat(val2.getOptions(), is(Nip50SearchTerm.Options.builder()
                .language(Language.ENGLISH)
                .nsfw(true)
                .includeSpam(false)
                .build()));

        Nip50SearchTerm val3 = Nip50SearchTerm.tryParse("unknown:test language:en nsfw:false include:spam sentiment:negative").orElseThrow();
        assertThat(val3.getSearch(), is(""));
        assertThat(val3.getOptions(), is(Nip50SearchTerm.Options.builder()
                .language(Language.ENGLISH)
                .nsfw(false)
                .includeSpam(true)
                .sentiment(-1)
                .build()));

        Nip50SearchTerm val4 = Nip50SearchTerm.tryParse("language:xx purple").orElseThrow();
        assertThat(val4.getSearch(), is("purple"));
        assertThat(val4.getOptions(), is(Nip50SearchTerm.Options.builder()
                .language(null)
                .build()));

        Nip50SearchTerm val5 = Nip50SearchTerm.tryParse("language:xx \"purple\" OR    !orange +text key:value -1 sentiment:positive @").orElseThrow();
        assertThat(val5.getSearch(), is("\"purple\" OR !orange +text -1 @"));
        assertThat(val5.getOptions(), is(Nip50SearchTerm.Options.builder()
                .language(null)
                .sentiment(1)
                .build()));
    }

    @Test
    void tryParse1Empty() {
        Nip50SearchTerm val0 = Nip50SearchTerm.tryParse("").orElseThrow();
        assertThat(val0.getSearch(), is(""));
        assertThat(val0.getOptions(), is(Nip50SearchTerm.Options.defaultOptions()));

        Nip50SearchTerm val1 = Nip50SearchTerm.tryParse("                ").orElseThrow();
        assertThat(val1.getSearch(), is(""));
        assertThat(val1.getOptions(), is(Nip50SearchTerm.Options.defaultOptions()));

        Nip50SearchTerm val2 = Nip50SearchTerm.tryParse("    \t \r \n    ").orElseThrow();
        assertThat(val2.getSearch(), is(""));
        assertThat(val2.getOptions(), is(Nip50SearchTerm.Options.defaultOptions()));

        Nip50SearchTerm val3 = Nip50SearchTerm.tryParse("    \t \r \n  language:it  ").orElseThrow();
        assertThat(val3.getSearch(), is(""));
        assertThat(val3.getOptions(), is(Nip50SearchTerm.Options.builder()
                .language(Language.ITALIAN)
                .build()));
    }
}