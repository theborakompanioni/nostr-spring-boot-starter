package org.tbk.nostr.nip19;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Nip19Type {
    NPUB("npub"),
    NSEC("nsec"),
    NOTE("note"),
    NPROFILE("nprofile"),
    NEVENT("nevent"),
    NADDR("naddr"),
    ;

    public static Nip19Type fromHrp(String hrp) {
        return Arrays.stream(Nip19Type.values())
                .filter(it -> it.getHrp().equals(hrp))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown hrp: %s".formatted(hrp)));
    }

    @NonNull
    private final String hrp;
}
