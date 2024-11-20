package org.tbk.nostr.nip19;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityType {
    NPUB("npub"),
    NSEC("nsec"),
    NOTE("note"),
    NPROFILE("nprofile"),
    NEVENT("nevent"),
    NADDR("naddr"),
    ;

    @NonNull
    private final String hrp;
}
