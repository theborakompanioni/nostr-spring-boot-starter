package org.tbk.nostr.example.shell;

import com.google.protobuf.ByteString;
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

@Slf4j
@ShellComponent
@ShellCommandGroup("Commands")
@RequiredArgsConstructor
class NostrShellCommands {

    @ShellMethod(key = "pow", value = "Generate NIP-13 Proof of Work Notes")
    public String pow(
            @ShellOption(value = "json", help = "note body") String json,
            @ShellOption(value = "target", defaultValue = "8", help = "target difficulty (default: 8)") int targetDifficultyArg
    ) {
        Event.Builder protoEvent = JsonReader.fromJsonPartial(json, Event.newBuilder()
                .setCreatedAt(System.currentTimeMillis() / 1_000L));

        Event.Builder builder = Nip13.mineEvent(protoEvent, targetDifficultyArg);

        return JsonWriter.toJson(builder.build());
    }
}