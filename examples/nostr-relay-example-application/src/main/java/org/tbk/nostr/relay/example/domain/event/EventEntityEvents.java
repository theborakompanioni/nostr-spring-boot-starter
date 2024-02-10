package org.tbk.nostr.relay.example.domain.event;

public final class EventEntityEvents {
    private EventEntityEvents() {
        throw new UnsupportedOperationException();
    }

    public record CreatedEvent(EventEntity.EventEntityId eventId) {
    }

    public record MarkDeletedEvent(EventEntity.EventEntityId eventId) {
    }
}
