package com.engineersbox.conduit.processing.event;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.riemann.riemann.Proto;

import java.io.IOException;

public class EventSerialiser extends StdSerializer<Proto.Event> {
    public EventSerialiser(final Class<Proto.Event> t) {
        super(t);
    }

    @Override
    public void serialize(final Proto.Event value,
                          final JsonGenerator gen,
                          final SerializerProvider provider) throws IOException {

    }
}
