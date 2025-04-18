package org.tbk.nostr.example.agentic.lib.jackson.datatype.nostr;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonWriter;

import java.io.IOException;

public class EventSerializer extends StdSerializer<Event> {
    protected EventSerializer(Class<Event> t) {
        super(t);
    }

    @Override
    public void serialize(Event event, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeRawValue(JsonWriter.toJson(event));
    }
}
