package org.tbk.nostr.client;

import org.springframework.web.socket.CloseStatus;

public interface OnCloseHandler {

    void doOnClose(NostrClientService service, CloseStatus closeStatus);
}
