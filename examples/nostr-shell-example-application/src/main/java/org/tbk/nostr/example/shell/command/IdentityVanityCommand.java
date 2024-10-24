package org.tbk.nostr.example.shell.command;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.nostr.example.shell.util.Json;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.nips.Nip19;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class IdentityVanityCommand {

    @ShellMethod(key = "identity-vanity", value = "Generate a vanity nostr key pair")
    public String run(
            @ShellOption(value = "npub-prefix", defaultValue = "", help = "npub prefix") String npubPrefixArg,
            @ShellOption(value = "parallelism", defaultValue = "0", help = "parallelism level (default: # of processors / 2)") int parallelismArg
    ) throws IOException {
        int parallelism = parallelismArg > 0 ? parallelismArg : Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        String npubPrefix = npubPrefixArg == null ? "" : npubPrefixArg;

        if (!hasValidBech32Chars().test(npubPrefix)) {
            throw new IllegalArgumentException("npub-prefix contains invalid bech32 chars");
        }

        log.debug("Starting identity-vanity (npub-prefix := {}, parallelism := {}", npubPrefix, parallelism);

        Stopwatch stopwatch = Stopwatch.createStarted();

        Identity.Account account = requireNonNull(randomAccountWithNpubHavingPrefix(npubPrefix, parallelism).block());

        log.debug("identity-vanity with npub-prefix '{}' took {}", npubPrefix, stopwatch.stop());

        return Json.jsonPretty.composeString()
                .startObject()
                .put("privateKey", account.getPrivateKey().toHex())
                .put("publicKey", account.getPublicKey().value.toHex())
                .put("nsec", Nip19.toNsec(account.getPrivateKey()))
                .put("npub", Nip19.toNpub(account.getPublicKey()))
                .end()
                .finish();
    }

    private static Predicate<String> hasValidBech32Chars() {
        // from https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
        return test -> !test.contains("1") &&
               !test.contains("b") &&
               !test.contains("i") &&
               !test.contains("o");
    }

    private static Predicate<Identity.Account> withNpubPrefix(String npubPrefix) {
        return account -> Nip19.toNpub(account.getPublicKey()).startsWith("npub1" + npubPrefix);
    }

    private Mono<Identity.Account> randomAccountWithNpubHavingPrefix(String npubPrefix, int parallelism) {
        return mineAccountMatching(parallelism, withNpubPrefix(npubPrefix));
    }

    private Mono<Identity.Account> mineAccountMatching(int parallelism, Predicate<Identity.Account> predicate) {
        return requireNonNull(Mono.firstWithValue(IntStream.range(0, parallelism)
                .mapToObj(Integer::toString)
                .map(groupLabel -> Mono.fromCallable(() -> {
                            log.debug("Starting thread for group {}", groupLabel);
                            Identity.Account result = mineAccount(predicate);
                            log.debug("Found result in thread of group {}", groupLabel);
                            return result;
                        })
                        .subscribeOn(Schedulers.parallel())
                        .doOnCancel(() -> {
                            log.debug("Cancelled thread for group {}", groupLabel);
                        }))
                .toList()));
    }

    private Identity.Account mineAccount(Predicate<Identity.Account> predicate) {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(new InterruptedException("Thread interrupted: Abort operation 'mineAccount'."));
            }

            Identity.Account account = randomAccount();
            if (predicate.apply(account)) {
                return account;
            }
        }
    }

    private Identity.Account randomAccount() {
        return MoreIdentities.random().deriveAccount(0L);
    }

}