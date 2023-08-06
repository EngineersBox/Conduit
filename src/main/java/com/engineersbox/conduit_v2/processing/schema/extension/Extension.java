package com.engineersbox.conduit_v2.processing.schema.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;

public interface Extension {

    String name();
    @SuppressWarnings("unchecked")
    default Class<? extends JsonDeserializer<? extends Extension>> deserializerClass() {
        return (Class<? extends JsonDeserializer<? extends Extension>>) JsonDeserializer.None.class;
    }
    default TypeReference<? extends Extension> targetType() {
        return null;
    }

}
