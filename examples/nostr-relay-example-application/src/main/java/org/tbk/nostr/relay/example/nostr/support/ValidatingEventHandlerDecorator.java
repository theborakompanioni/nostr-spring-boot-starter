package org.tbk.nostr.relay.example.nostr.support;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.nostr.handler.EventRequestHandler;
import org.tbk.nostr.relay.example.nostr.support.validating.EventValidator;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ValidatingEventHandlerDecorator implements EventRequestHandler {

    @NonNull
    private final EventRequestHandler delegate;

    @NonNull
    private final List<EventValidator> validators;

    @Override
    public void handleEventMessage(WebSocketSession session, EventRequest request) throws Exception {
        Event event = request.getEvent();
        BindException errors = new BindException(event, "event");

        for (EventValidator validator : validators) {
            ValidationUtils.invokeValidator(validator, event, errors);

            if (errors.hasErrors()) {
                break;
            }
        }

        if (!errors.hasErrors()) {
            delegate.handleEventMessage(session, request);
        } else {
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
        }
    }
}
