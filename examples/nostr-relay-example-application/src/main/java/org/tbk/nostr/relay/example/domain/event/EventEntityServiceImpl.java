package org.tbk.nostr.relay.example.domain.event;

import com.google.common.collect.ImmutableList;
import fr.acinq.bitcoin.XonlyPublicKey;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties.RelayOptionsProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@Transactional
public class EventEntityServiceImpl implements EventEntityService {
    private static final Sort sortByCreatedAtDesc = Sort.by(Sort.Direction.DESC, "createdAt");

    private static final Comparator<EventEntity> compareByCreatedAtDesc = Comparator.
            <EventEntity>comparingLong(it -> it.getCreatedAt().getEpochSecond())
            .reversed();

    private final EventEntities events;

    private final PageRequest defaultPageRequest;

    public EventEntityServiceImpl(EventEntities events, RelayOptionsProperties relayOptions) {
        requireNonNull(relayOptions);
        this.events = requireNonNull(events);
        this.defaultPageRequest = PageRequest.of(0, relayOptions.getInitialQueryLimit(), sortByCreatedAtDesc);
    }

    @Override
    public EventEntity createEvent(Event event) {
        return events.save(new EventEntity(event));
    }

    @Override
    public Mono<EventEntity> findById(EventEntity.EventEntityId eventId) {
        return Mono.justOrEmpty(events.findById(eventId));
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
    public Flux<EventId> markDeleted(Collection<EventId> deletableEventIds, XonlyPublicKey author) {
        Specification<EventEntity> deletionSpecification = Specification.anyOf(deletableEventIds.stream()
                        .map(EventEntitySpecifications::hasId)
                        .collect(Collectors.toList()))
                .and(EventEntitySpecifications.hasPubkey(author))
                .and(Specification.not(EventEntitySpecifications.hasKind(5)))
                .and(EventEntitySpecifications.isNotDeleted());

        List<EventEntity> deletableEvents = events.findAll(deletionSpecification);

        Instant now = Instant.now();
        List<EventEntity> deletabledEntities = events.saveAll(deletableEvents.stream().map(it -> it.markDeleted(now)).toList());

        return Flux.fromIterable(deletabledEntities)
                .map(it -> EventId.fromHex(it.getId().getId()));
    }

    @Override
    public EventEntity markExpiresAt(EventId eventId, Instant expiresAt) {
        EventEntity entity = events.findById(EventEntity.EventEntityId.of(eventId.toHex()))
                .orElseThrow(() -> new EntityNotFoundException("Unable to find event with id " + eventId.toHex()));

        return events.save(entity.markExpiresAt(expiresAt));
    }

    @Override
    public Flux<EventEntity> findAll(Collection<Filter> filters) {
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
                    .map(EventEntitySpecifications::fromFilter)
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
                        EventEntitySpecifications.fromFilter(filter),
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
            return Flux.fromStream(stream);
        } else {
            return Flux.fromStream(stream.distinct().sorted(compareByCreatedAtDesc));
        }
    }

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
