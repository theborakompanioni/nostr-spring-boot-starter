package org.tbk.nostr.nip19;

import org.tbk.nostr.base.EventId;

class NoteCodec implements Codec<EventId> {
    @Override
    public boolean supports(String hrp, Class<?> clazz) {
        return EntityType.NOTE.getHrp().equals(hrp) && clazz.isAssignableFrom(EventId.class);
    }

    @Override
    public EventId decode(String hrp, byte[] data) {
        return EventId.of(data);
    }
}
