package com.engineersbox.conduit.schema.provider;

import com.jayway.jsonpath.spi.mapper.*;

import java.util.function.Supplier;

public enum MappingProvider {
    GSON(GsonMappingProvider::new),
    JACKSON(JacksonMappingProvider::new),
    JAKARTA(JakartaMappingProvider::new),
    JSON_ORG(JsonOrgMappingProvider::new),
    JSON_SMART(JsonSmartMappingProvider::new),
    TAPESTRY(TapestryMappingProvider::new);

    private final Supplier<? extends com.jayway.jsonpath.spi.mapper.MappingProvider> constructor;

    MappingProvider(final Supplier<? extends com.jayway.jsonpath.spi.mapper.MappingProvider> constructor) {
        this.constructor = constructor;
    }

    public com.jayway.jsonpath.spi.mapper.MappingProvider getNewProvider() {
        return constructor.get();
    }

}
