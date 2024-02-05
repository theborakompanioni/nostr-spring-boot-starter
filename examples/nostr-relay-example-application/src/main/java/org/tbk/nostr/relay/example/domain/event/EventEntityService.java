package org.tbk.nostr.relay.example.domain.event;

import org.tbk.nostr.proto.Event;

public interface EventEntityService {

    EventEntity createEvent(Event event);
}