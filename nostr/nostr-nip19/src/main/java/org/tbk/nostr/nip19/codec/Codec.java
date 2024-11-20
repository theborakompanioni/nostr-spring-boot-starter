package org.tbk.nostr.nip19.codec;

public interface Codec<T> {
    boolean supports(String hrp, Class<?> clazz);

    T decode(String hrp, byte[] data);

    byte[] encode(String hrp, Object data);
}
