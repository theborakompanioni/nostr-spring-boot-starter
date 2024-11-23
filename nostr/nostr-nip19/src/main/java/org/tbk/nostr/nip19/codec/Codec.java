package org.tbk.nostr.nip19.codec;

import org.tbk.nostr.nip19.Nip19Entity;

public interface Codec<T extends Nip19Entity> {
    boolean supports(Class<? extends Nip19Entity> clazz);

    T decode(byte[] data);

    byte[] encode(Nip19Entity data);
}
