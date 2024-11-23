package org.tbk.nostr.nip19.codec;

import org.tbk.nostr.base.EventId;
import org.tbk.nostr.nip19.Nip19Entity;
import org.tbk.nostr.nip19.Note;

public class NoteCodec implements Codec<Note> {
    @Override
    public boolean supports(Class<? extends Nip19Entity> clazz) {
        return clazz.isAssignableFrom(Note.class);
    }

    @Override
    public Note decode(byte[] data) {
        return Note.builder().eventId(EventId.of(data)).build();
    }

    @Override
    public byte[] encode(Nip19Entity data) {
        if (!supports(data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        return ((Note) data).getEventId().toByteArray();
    }
}
