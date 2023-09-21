package com.engineersbox.conduit.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ObjectMapperModule {

    private ObjectMapperModule() {
        throw new UnsupportedOperationException("Static utility class");
    }

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

}
