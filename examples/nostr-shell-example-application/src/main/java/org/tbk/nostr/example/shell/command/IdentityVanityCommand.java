package org.tbk.nostr.example.shell.command;

import com.google.common.base.Stopwatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.nostr.example.shell.util.Json;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.nip19.Nip19;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Predicate;
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
            @ShellOption(value = "npub-suffix", defaultValue = "", help = "npub suffix") String npubSuffixArg,
            @ShellOption(value = "parallelism", defaultValue = "0", help = "parallelism level (default: # of processors / 2)") int parallelismArg,
            @ShellOption(value = "timeout", defaultValue = "-1", help = "timeout (e.g. 2s, 2d, default: -1 [no timeout])") String timeoutArg
    ) throws IOException {

        int parallelism = parallelismArg > 0 ? parallelismArg : Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        String npubPrefix = npubPrefixArg == null ? "" : npubPrefixArg;
        String npubSuffix = npubSuffixArg == null ? "" : npubSuffixArg;
        Optional<Duration> timeout = parseTimeout(timeoutArg);

        if (!hasValidBech32Chars().test(npubPrefix)) {
            throw new IllegalArgumentException("npub-prefix contains invalid bech32 chars");
        }
        if (!hasValidBech32Chars().test(npubSuffix)) {
            throw new IllegalArgumentException("npub-suffix contains invalid bech32 chars");
        }

        log.debug("Starting identity-vanity (npub-prefix := {}, npub-suffix := {}, parallelism := {}", npubPrefix, npubSuffix, parallelism);

        Stopwatch stopwatch = Stopwatch.createStarted();

        Mono<Identity.Account> accountMono = mineAccountMatching(parallelism, withNpubPrefixAndSuffix(npubPrefix, npubSuffix));
        Identity.Account account = requireNonNull(timeout.isEmpty() ? accountMono.block() : accountMono.block(timeout.get()));

        log.debug("identity-vanity with npub-prefix '{}' took {}", npubPrefix, stopwatch.stop());

        return Json.jsonPretty.composeString()
                .startObject()
                .put("privateKey", account.getPrivateKey().toHex())
                .put("publicKey", account.getPublicKey().value.toHex())
                .put("nsec", Nip19.encodeNsec(account.getPrivateKey()))
                .put("npub", Nip19.encodeNpub(account.getPublicKey()))
                .end()
                .finish();
    }

    private static Optional<Duration> parseTimeout(String timeoutArg) {
        Duration duration = DurationStyle.SIMPLE.parse(timeoutArg, ChronoUnit.SECONDS);
        return duration.isNegative() ? Optional.empty() : Optional.of(duration);
    }

    private static Predicate<String> hasValidBech32Chars() {
        // from https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
        return test -> !test.contains("1") &&
                       !test.contains("b") &&
                       !test.contains("i") &&
                       !test.contains("o");
    }

    private static Predicate<Identity.Account> withNpubPrefixAndSuffix(String prefix, String suffix) {
        Predicate<String> prefixPredicate = npub -> npub.startsWith("npub1" + prefix);
        Predicate<String> suffixPredicate = npub -> npub.endsWith(suffix);
        return withNpubMatching(prefix.isEmpty() ? suffixPredicate : prefixPredicate.and(suffixPredicate));
    }

    private static Predicate<Identity.Account> withNpubMatching(Predicate<String> matcher) {
        return account -> matcher.test(Nip19.encodeNpub(account.getPublicKey()));
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
            if (predicate.test(account)) {
                return account;
            }
        }
    }

    private Identity.Account randomAccount() {
        return MoreIdentities.random().deriveAccount(0L);
    }

}