package org.tbk.nostr.example.relay.domain.event;

public final class EventEntityEvents {
    private EventEntityEvents() {
        throw new UnsupportedOperationException();
    }

    public record CreatedEvent(EventEntity.EventEntityId eventId) {
    }

    public record MarkDeletedEvent(EventEntity.EventEntityId eventId) {
    }
}
