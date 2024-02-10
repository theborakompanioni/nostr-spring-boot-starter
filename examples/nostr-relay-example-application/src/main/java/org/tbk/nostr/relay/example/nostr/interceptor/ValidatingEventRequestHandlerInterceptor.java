package org.tbk.nostr.relay.example.nostr.interceptor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.nostr.validating.EventValidator;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ValidatingEventRequestHandlerInterceptor implements NostrRequestHandlerInterceptor {

    @NonNull
    private final List<EventValidator> validators;

    @Override
    public boolean preHandle(WebSocketSession session, Request request) throws Exception {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            return handleEventMessage(session, request.getEvent());
        }

        return true;
    }

    public boolean handleEventMessage(WebSocketSession session, EventRequest request) throws Exception {
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

        session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                .setOk(OkResponse.newBuilder()
                        .setEventId(event.getId())
                        .setSuccess(false)
                        .setMessage("Error: %s".formatted(message))
                        .build())
                .build())));

        return false;
    }
}
