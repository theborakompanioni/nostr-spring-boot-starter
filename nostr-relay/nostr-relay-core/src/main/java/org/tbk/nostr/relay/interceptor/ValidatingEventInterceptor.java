package org.tbk.nostr.relay.interceptor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.validation.EventValidator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
public class ValidatingEventInterceptor implements RequestHandlerInterceptor {

    @NonNull
    private final List<EventValidator> validators;

    @NonNull
    private final MessageSource messageSource;

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

            // skip remaining validators early in case of any error
            if (errors.hasErrors()) {
                break;
            }
        }

        if (!errors.hasErrors()) {
            return true;
        }

        String message = errors.getAllErrors().stream().findFirst()
                .map(it -> messageSource.getMessage(it, Locale.getDefault()))
                .orElse("Invalid event.");


        log.debug("Validation of event {} failed: {}", event.getId(), message);

        String messageWithErrorPrefix = withOkResponseErrorMessagePrefixIfNecessary(message, "invalid");

        context.add(Response.newBuilder()
                .setOk(OkResponse.newBuilder()
                        .setEventId(event.getId())
                        .setSuccess(false)
                        .setMessage(messageWithErrorPrefix)
                        .build())
                .build());

        return false;
    }

    private String withOkResponseErrorMessagePrefixIfNecessary(String errorMessage, String defaultPrefix) {
        return hasOkResponseErrorMessagePrefix(errorMessage) ? errorMessage : String.format("%s: %s", defaultPrefix, errorMessage);
    }

    private boolean hasOkResponseErrorMessagePrefix(String errorMessage) {
        return errorMessage.startsWith("error:")
               || errorMessage.startsWith("invalid:")
               || errorMessage.startsWith("duplicate:")
               || errorMessage.startsWith("rate-limited:")
               || errorMessage.startsWith("pow:")
               || errorMessage.startsWith("blocked:");
    }
}
