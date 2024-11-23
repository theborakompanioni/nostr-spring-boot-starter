package org.tbk.nostr.nip19;

public sealed interface Nip19Entity permits Note, Nsec, Npub, Naddr, Nevent, Nprofile {

    Nip19Type getEntityType();
}