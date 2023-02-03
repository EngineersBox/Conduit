package com.engineersbox.conduit.schema.provider;

import com.jayway.jsonpath.spi.json.*;

import java.util.function.Supplier;

public enum JsonProvider {
    GSON(GsonJsonProvider::new),
    JACKSON(JacksonJsonProvider::new),
    JAKARTA(JakartaJsonProvider::new),
    JSON_ORG(JsonOrgJsonProvider::new),
    JSON_SMART(JsonSmartJsonProvider::new),
    TAPESTRY(TapestryJsonProvider::new);

    private final Supplier<? extends com.jayway.jsonpath.spi.json.JsonProvider> constructor;

    JsonProvider(final Supplier<? extends com.jayway.jsonpath.spi.json.JsonProvider> constructor) {
        this.constructor = constructor;
    }

    public com.jayway.jsonpath.spi.json.JsonProvider getNewProvider() {
        return this.constructor.get();
    }

}
