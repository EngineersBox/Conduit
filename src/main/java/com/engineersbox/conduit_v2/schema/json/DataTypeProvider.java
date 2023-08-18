package com.engineersbox.conduit_v2.schema.json;

import com.jayway.jsonpath.spi.json.*;
import com.jayway.jsonpath.spi.mapper.*;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

import java.util.function.Supplier;

public final class DataTypeProvider {

    // Make these thread safe with RW locks
    static final MutableMap<String, Supplier<? extends JsonProvider>> JSON_PROVIDERS;
    static final MutableMap<String, Supplier<? extends MappingProvider>> MAPPING_PROVIDERS;

    static {
        JSON_PROVIDERS = Maps.mutable.<String, Supplier<? extends JsonProvider>>of()
                .withKeyValue("JACKSON", JacksonJsonProvider::new)
                .withKeyValue("GSON", GsonJsonProvider::new);
//                .withKeyValue("JAKARTA", JakartaJsonProvider::new)
//                .withKeyValue("JSON_ORG", JsonOrgJsonProvider::new)
//                .withKeyValue("JSON_SMART", JsonSmartJsonProvider::new)
//                .withKeyValue("TAPESTRY", TapestryJsonProvider::new);
        MAPPING_PROVIDERS = Maps.mutable.<String, Supplier<? extends MappingProvider>>of()
                .withKeyValue("JACKSON", JacksonMappingProvider::new)
                .withKeyValue("GSON", GsonMappingProvider::new);
//                .withKeyValue("JAKARTA", JakartaMappingProvider::new)
//                .withKeyValue("JSON_ORG", JsonOrgMappingProvider::new)
//                .withKeyValue("JSON_SMART", JsonSmartMappingProvider::new)
//                .withKeyValue("TAPESTRY", TapestryMappingProvider::new);
    }

    public static void bindJsonProvider(final String name,
                                        final Supplier<? extends JsonProvider> provider) {
        if (JSON_PROVIDERS.containsKey(name)) {
            throw new IllegalArgumentException("Json provider has already been bound: " + name);
        }
        JSON_PROVIDERS.put(name, provider);
    }

    public static void bindMappingProvider(final String name,
                                           final Supplier<? extends MappingProvider> provider) {
        if (MAPPING_PROVIDERS.containsKey(name)) {
            throw new IllegalArgumentException("Mapping provider has already been bound: " + name);
        }
        MAPPING_PROVIDERS.put(name, provider);
    }

}
