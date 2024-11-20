package org.tbk.nostr.nip19;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum TlvType {
    SPECIAL((byte) 0),
    RELAY((byte) 1),
    AUTHOR((byte) 2),
    KIND((byte) 3);

    private final byte value;
}
