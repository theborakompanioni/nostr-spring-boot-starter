package org.tbk.nostr.example.agentic.lib.jackson.datatype.nostr;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import org.tbk.nostr.proto.Event;

public class NostrModule extends Module {
    @Override
    public String getModuleName() {
        return "NostrModule";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(new EventSerializer(Event.class));
        context.addSerializers(serializers);
    }
}
