package org.tbk.nostr.persona;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.nip19.Nip19;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PersonaTest {

    @Test
    void alice() {
        Identity alice = Persona.alice();
        assertThat(Nip19.encodeNsec(alice.deriveAccount(0)), is("nsec1064t9a0fxkd6mdfcwghz8ehxtwcvsfj6wp7nzlkykyeve536adeqjksgqj"));
        assertThat(Nip19.encodeNpub(alice.deriveAccount(0)), is("npub17vvjdx582l5yaxmd4kfjtjm5jvlkf62f03xr4rmh2umpu78d74jqxhkuj6"));

        assertThat(Nip19.encodeNsec(alice.deriveAccount(16)), is("nsec1yyx8p768cv5ld0x98p9p8lt2lj939l5sec8sgujfseen89fwlfwsjyjc64"));
        assertThat(Nip19.encodeNpub(alice.deriveAccount(16)), is("npub1l493jqgqzjtpcvygmlcntnnu77n0v4s3c4n79sdl78rg4zwkth9q6uy822"));
    }

    @Test
    void bob() {
        Identity bob = Persona.bob();
        assertThat(Nip19.encodeNsec(bob.deriveAccount(0)), is("nsec1mdf536ez4wcz8l0xq9d9v2jk5zw6dm3ytrrhzn95tzhc6azpum3s9xd9wz"));
        assertThat(Nip19.encodeNpub(bob.deriveAccount(0)), is("npub1wfsrhzsn98xeedlpzlcan44wdwunshk9j8fs50y3aaq24z4ycsysuspqy8"));

        assertThat(Nip19.encodeNsec(bob.deriveAccount(16)), is("nsec14kwtux4mju4xc6f5nq3x4rj0unx32v6e49hhhwrwdtnd33phranqdt3grs"));
        assertThat(Nip19.encodeNpub(bob.deriveAccount(16)), is("npub1p48392yeyexd498svuhyf802yh3yvqu6gg3t8z6fmjr0u607hmwsmat3x8"));
    }
}