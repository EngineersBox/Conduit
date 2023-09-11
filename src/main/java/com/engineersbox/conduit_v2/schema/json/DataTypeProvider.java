package com.engineersbox.conduit_v2.schema.json;

import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import java.util.function.Supplier;

public final class DataTypeProvider {

    static final ConcurrentMutableMap<String, Supplier<? extends JsonProvider>> JSON_PROVIDERS;
    static final ConcurrentMutableMap<String, Supplier<? extends MappingProvider>> MAPPING_PROVIDERS;

    // TODO: Uncomment rest of these and add dependencies
    static {
        JSON_PROVIDERS = ConcurrentHashMap.<String, Supplier<? extends JsonProvider>>newMap()
                .withKeyValue("JACKSON", JacksonJsonProvider::new)
                .withKeyValue("GSON", GsonJsonProvider::new);
//                .withKeyValue("JAKARTA", JakartaJsonProvider::new)
//                .withKeyValue("JSON_ORG", JsonOrgJsonProvider::new)
//                .withKeyValue("JSON_SMART", JsonSmartJsonProvider::new)
//                .withKeyValue("TAPESTRY", TapestryJsonProvider::new);
        MAPPING_PROVIDERS = ConcurrentHashMap.<String, Supplier<? extends MappingProvider>>newMap()
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
