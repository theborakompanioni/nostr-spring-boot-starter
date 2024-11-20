package org.tbk.nostr.nip19.codec;

import org.tbk.nostr.base.EventId;
import org.tbk.nostr.nip19.EntityType;

public class NoteCodec implements Codec<EventId> {
    @Override
    public boolean supports(String hrp, Class<?> clazz) {
        return EntityType.NOTE.getHrp().equals(hrp) && clazz.isAssignableFrom(EventId.class);
    }

    @Override
    public EventId decode(String hrp, byte[] data) {
        return EventId.of(data);
    }

    @Override
    public byte[] encode(String hrp, Object data) {
        if (!supports(hrp, data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        return ((EventId) data).toByteArray();
    }
}
