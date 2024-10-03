package org.tbk.nostr.identity;

import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.bitcoin.KeyPath;
import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.*;
import org.tbk.nostr.nip6.Nip6;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Identity {

    @Value
    @Builder
    public static class Account {
        @NonNull
        KeyPath path;
        @NonNull
        PrivateKey privateKey;

        public XonlyPublicKey getPublicKey() {
            return privateKey.xOnlyPublicKey();
        }
    }

    @NonNull
    private final DeterministicWallet.ExtendedPrivateKey masterPrivateKey;

    public Account deriveAccount(long account) {
        KeyPath keyPath = Nip6.keyPath(account);
        PrivateKey privateKey = Nip6.fromMasterPrivateKey(masterPrivateKey, keyPath);

        return Account.builder()
                .path(keyPath)
                .privateKey(privateKey)
                .build();
    }
}
