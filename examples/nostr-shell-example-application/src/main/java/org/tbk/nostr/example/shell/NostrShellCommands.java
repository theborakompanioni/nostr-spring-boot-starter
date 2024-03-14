package org.tbk.nostr.example.shell;

import com.google.common.base.Stopwatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.tbk.nostr.nips.Nip13;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class NostrShellCommands {

    @ShellMethod(key = "pow", value = "Generate NIP-13 Proof of Work Notes")
    public String pow(
            @ShellOption(value = "json", help = "note body") String json,
            @ShellOption(value = "target", defaultValue = "8", help = "target difficulty (default: 8)") int targetDifficultyArg,
            @ShellOption(value = "parallelism", defaultValue = "0", help = "parallelism level (default: # of processors / 2)") int parallelismArg
    ) {
        int parallelism = parallelismArg > 0 ? parallelismArg : Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        int difficultyTarget = targetDifficultyArg > 0 ? targetDifficultyArg : 8;

        Event.Builder protoEvent = JsonReader.fromJsonPartial(json, Event.newBuilder()
                .setCreatedAt(System.currentTimeMillis() / 1_000L));

        log.debug("Starting pow (difficulty := {}, parallelism := {}", difficultyTarget, parallelism);

        Stopwatch stopwatch = Stopwatch.createStarted();
        Event.Builder builder = requireNonNull(Mono.firstWithValue(IntStream.range(0, parallelism)
                .mapToObj(Integer::toString)
                .map(groupLabel -> Mono.fromCallable(() -> {
                            log.debug("Starting pow thread for group {}", groupLabel);
                            Event.Builder result = Nip13.mineEvent(protoEvent, difficultyTarget, groupLabel);
                            log.debug("Found pow result in thread of group {}", groupLabel);
                            return result;
                        })
                        .subscribeOn(Schedulers.parallel())
                        .doOnCancel(() -> {
                            log.debug("Cancelled pow thread for group {}", groupLabel);
                        }))
                .toList()))
                .block();

        log.debug("pow with difficulty target {} took {}", difficultyTarget, stopwatch.stop());

        return JsonWriter.toJson(builder.build());
    }
}