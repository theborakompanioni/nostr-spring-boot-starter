package org.tbk.nostr.nip19;

interface Codec<T> {
    boolean supports(String hrp, Class<?> clazz);

    T decode(String hrp, byte[] data);
}
