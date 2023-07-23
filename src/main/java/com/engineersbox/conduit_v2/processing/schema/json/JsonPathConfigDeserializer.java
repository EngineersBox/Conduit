package com.engineersbox.conduit_v2.processing.schema.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.jayway.jsonpath.Configuration;

import java.io.IOException;

public class JsonPathConfigDeserializer extends StdDeserializer<Configuration> {

    protected JsonPathConfigDeserializer() {
        this(Configuration.class);
    }

    protected JsonPathConfigDeserializer(final Class<?> vc) {
        super(vc);
    }

    protected JsonPathConfigDeserializer(final JavaType valueType) {
        super(valueType);
    }

    protected JsonPathConfigDeserializer(final StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public Configuration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return null;
    }
}
