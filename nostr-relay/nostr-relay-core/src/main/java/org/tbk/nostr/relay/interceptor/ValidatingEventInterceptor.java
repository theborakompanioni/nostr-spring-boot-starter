package org.tbk.nostr.relay.interceptor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.validation.EventValidator;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ValidatingEventInterceptor implements RequestHandlerInterceptor {

    @NonNull
    private final List<EventValidator> validators;

    @Override
    public boolean preHandle(NostrRequestContext context, Request request) {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            return handleEventMessage(context, request.getEvent());
        }

        return true;
    }

    private boolean handleEventMessage(NostrRequestContext context, EventRequest request) {
        Event event = request.getEvent();
        BindException errors = new BindException(event, "event");

        for (EventValidator validator : validators) {
            ValidationUtils.invokeValidator(validator, event, errors);

            if (errors.hasErrors()) {
                break;
            }
        }

        if (!errors.hasErrors()) {
            return true;
        }

        String message = errors.getAllErrors().stream().findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Invalid event.");

        log.debug("Validation of event {} failed: {}", event.getId(), message);

        context.add(Response.newBuilder()
                .setOk(OkResponse.newBuilder()
                        .setEventId(event.getId())
                        .setSuccess(false)
                        .setMessage("Error: %s".formatted(message))
                        .build())
                .build());

        return false;
    }
}
