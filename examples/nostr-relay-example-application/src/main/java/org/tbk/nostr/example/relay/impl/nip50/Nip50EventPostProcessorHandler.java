package org.tbk.nostr.example.relay.impl.nip50;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.example.relay.domain.event.EventEntity;
import org.tbk.nostr.example.relay.domain.event.EventEntityService;
import org.tbk.nostr.example.relay.domain.event.EventNip50MetaInfoEntity;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class Nip50EventPostProcessorHandler implements RequestHandlerInterceptor {

    @NonNull
    private final EventEntityService eventEntityService;

    @NonNull
    private final LanguageDetector languageDetector;

    @Override
    public void postHandle(NostrRequestContext context, Request request) {
        context.getHandledEvent().ifPresent(this::process);
    }

    private void process(Event event) {
        // only support text notes in the example application
        if (event.getKind() != 1) {
            return;
        }
        if (event.getContent().isBlank()) {
            return;
        }

        // TODO: possible add "alt", "summary" and "title" tags
        //   See: https://github.com/nostr-protocol/nips?#standardized-tags

        Language language = languageDetector.detectLanguageOf(event.getContent());
        if (language == Language.UNKNOWN) {
            log.debug("Skip storing full-text search info (NIP-50) for event: Unknown language.");
            return;
        }
        if (log.isTraceEnabled()) {
            log.trace("Storing full-text search info (NIP-50) for event: language := '{}'", language);
        }

        EventEntity.EventEntityId eventEntityId = EventEntity.EventEntityId.fromEvent(event);
        EventNip50MetaInfoEntity nip50EventMetaInfo = new EventNip50MetaInfoEntity(
                eventEntityId,
                language,
                event.getContent());
        eventEntityService.addNip50MetaInfo(nip50EventMetaInfo);
    }
}
