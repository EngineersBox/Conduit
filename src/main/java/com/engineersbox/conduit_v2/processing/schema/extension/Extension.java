package com.engineersbox.conduit_v2.processing.schema.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;

public interface Extension extends ExtensionSchemaPatch {

    String name();

    default Class<? extends JsonDeserializer<?>> deserializerClass() {
        return JsonDeserializer.None.class;
    }

    TypeReference<?> targetType();

}
