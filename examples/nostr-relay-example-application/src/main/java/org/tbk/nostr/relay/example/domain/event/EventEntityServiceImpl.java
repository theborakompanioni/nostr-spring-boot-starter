package org.tbk.nostr.relay.example.domain.event;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties.RelayOptionsProperties;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@Transactional
public class EventEntityServiceImpl implements EventEntityService {

    private static final Sort sortByCreatedAtDesc = Sort.by(Sort.Direction.DESC, "createdAt");

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
    public Flux<EventEntity> find(List<Filter> filters) {
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

        return Flux.fromStream(streamBuilder.build().stream()
                .flatMap(it -> it)
                .distinct());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(EventEntityEvents.CreatedEvent created) {
        EventEntity entity = events.findById(created.eventId())
                .orElseThrow(() -> new IllegalStateException("Could not find EventEntity from CreatedEvent"));

        log.trace("Successfully saved event {}", entity.getId().getId());

        if (entity.isExpired(Instant.now())) {
            events.save(entity.markDeleted());
        }
    }

    @Async
    void on(EventEntityEvents.MarkDeletedEvent markDeleted) {
        log.debug("Successfully marked event as deleted: {} ", markDeleted.eventId().getId());
    }
}
