package org.tbk.nostr.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class ReconnectOnClose implements OnCloseHandler {

    @NonNull
    private final Duration delay;


    @NonNull
    private final Duration maxDelay;

    public ReconnectOnClose() {
        this(Duration.ofSeconds(5));
    }

    public ReconnectOnClose(Duration delay) {
        this(delay, Duration.ofMinutes(2));
    }

    @Override
    public void doOnClose(NostrClientService service, CloseStatus closeStatus) {
        scheduleReconnect(service, Duration.ofSeconds(5));
    }

    private void scheduleReconnect(NostrClientService service, Duration delay) {
        service.reconnect(delay)
                .subscribe(success -> {
                    log.info("Reconnect attempt returned: {}", success);
                }, e -> {
                    log.warn("Error while reconnecting to relay: {}", e.getMessage());
                    Duration proposedNewDelay = delay.plus(delay);
                    Duration maxDelay = Duration.ofMinutes(2);
                    Duration newDelay = proposedNewDelay.compareTo(maxDelay) > 0 ? maxDelay : proposedNewDelay;
                    scheduleReconnect(service, newDelay);
                });
    }
}
