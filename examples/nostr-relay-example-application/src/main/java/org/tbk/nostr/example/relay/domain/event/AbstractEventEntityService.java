package org.tbk.nostr.example.relay.domain.event;

import com.google.common.collect.ImmutableList;
import fr.acinq.bitcoin.XonlyPublicKey;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.example.relay.NostrRelayExampleApplicationProperties;
import org.tbk.nostr.example.relay.domain.event.EventEntity.EventEntityId;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Slf4j
@Transactional
abstract class AbstractEventEntityService implements EventEntityService {
    private static final Sort sortByCreatedAtDesc = Sort.by(Sort.Direction.DESC, "createdAt");

    private static final Comparator<EventEntity> compareByCreatedAtDesc = Comparator.
            <EventEntity>comparingLong(it -> it.getCreatedAt().getEpochSecond())
            .reversed();

    private final EventEntities events;

    private final PageRequest defaultPageRequest;

    AbstractEventEntityService(EventEntities events,
                               NostrRelayExampleApplicationProperties properties) {
        requireNonNull(properties);
        this.events = requireNonNull(events);
        this.defaultPageRequest = PageRequest.of(0, properties.getInitialQueryLimit(), sortByCreatedAtDesc);
    }

    @Override
    public EventEntity createEvent(Event event) {
        return events.save(new EventEntity(event));
    }

    @Override
    public Optional<EventEntity> findById(EventEntityId eventId) {
        return events.findById(eventId);
    }

    @Override
    public Page<EventEntity> findAll(Specification<EventEntity> specs, Pageable page) {
        return events.findAll(specs, page);
    }

    @Override
    public boolean exists(Specification<EventEntity> specs) {
        return events.exists(specs);
    }

    @Override
    public List<EventId> markDeleted(XonlyPublicKey author, Specification<EventEntity> specs) {
        List<EventEntity> deletableEvents = events.findAll(specs
                .and(EventEntitySpecifications.hasPubkey(author))
                .and(Specification.not(EventEntitySpecifications.hasKind(Nip9.kind())))
                .and(EventEntitySpecifications.isNotDeleted()));

        Instant now = Instant.now();
        List<EventEntity> deletabledEntities = events.saveAll(deletableEvents.stream().map(it -> it.markDeleted(now)).toList());

        return deletabledEntities.stream()
                .map(it -> EventId.fromHex(it.getId().getId()))
                .toList();
    }

    @Override
    public EventEntity markExpiresAt(EventId eventId, Instant expiresAt) {
        EventEntity entity = events.findById(EventEntityId.of(eventId.toHex()))
                .orElseThrow(() -> new EntityNotFoundException("Unable to find event with id " + eventId.toHex()));

        return events.save(entity.markExpiresAt(expiresAt));
    }

    @Override
    public EventEntity addNip50MetaInfo(EventNip50MetaInfoEntity nip50EventMetaInfo) {
        EventEntity entity = events.findById(EventEntityId.of(nip50EventMetaInfo.getEventId()))
                .orElseThrow(() -> new EntityNotFoundException("Unable to find event with id " + nip50EventMetaInfo.getEventId()));

        return events.save(entity.addNip50MetaInfo(nip50EventMetaInfo));
    }

    @Override
    public List<EventEntity> findAll(Collection<Filter> filters) {
        List<Filter> filterWithoutLimit = filters.stream()
                .filter(it -> !it.hasField(Filter.getDescriptor().findFieldByNumber(Filter.LIMIT_FIELD_NUMBER)))
                .toList();
        List<Filter> filterWithLimit = filters.stream()
                .filter(it -> it.hasField(Filter.getDescriptor().findFieldByNumber(Filter.LIMIT_FIELD_NUMBER)))
                .toList();

        ImmutableList.Builder<Stream<EventEntity>> streamBuilder = ImmutableList.builder();

        // filters without limits can be combined
        if (!filterWithoutLimit.isEmpty()) {
            List<Specification<EventEntity>> filterSpecifications = filterWithoutLimit.stream()
                    .map(this::fromFilter)
                    .toList();

            Specification<EventEntity> specification = Specification.allOf(
                    Specification.anyOf(filterSpecifications),
                    EventEntitySpecifications.isNotDeleted(),
                    EventEntitySpecifications.isNotExpired()
            );

            Page<EventEntity> page = events.findAll(specification, defaultPageRequest);
            streamBuilder.add(page.stream());
        }

        // filter with limits must have their own queries (for the custom limit to be applied)
        if (!filterWithLimit.isEmpty()) {
            for (Filter filter : filterWithLimit) {
                Specification<EventEntity> specification = Specification.allOf(
                        fromFilter(filter),
                        EventEntitySpecifications.isNotDeleted(),
                        EventEntitySpecifications.isNotExpired()
                );
                PageRequest pageRequest = PageRequest.of(0, filter.getLimit(), sortByCreatedAtDesc);
                Page<EventEntity> page = events.findAll(specification, pageRequest);
                streamBuilder.add(page.stream());
            }
        }

        Stream<EventEntity> stream = streamBuilder.build().stream()
                .reduce(Stream.empty(), Stream::concat);

        if (filterWithLimit.isEmpty()) {
            return stream.toList();
        } else {
            return stream.distinct()
                    .sorted(compareByCreatedAtDesc)
                    .toList();
        }
    }

    protected Specification<EventEntity> fromFilter(Filter filter) {
        return EventEntitySpecifications.fromFilter(filter).and(search(filter));
    }

    protected abstract Specification<EventEntity> search(Filter filter);

    @Async
    @EventListener
    void on(EventEntityEvents.CreatedEvent created) {
        log.trace("Successfully saved event {}", created.eventId().getId());
    }

    @Async
    @EventListener
    void on(EventEntityEvents.MarkDeletedEvent markDeleted) {
        log.debug("Successfully marked event as deleted: {} ", markDeleted.eventId().getId());
    }
}
