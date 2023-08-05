package com.engineersbox.conduit_v2.processing.schema.extension;

import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

public class ExtensionProvider {

    private static final ConcurrentMutableMap<String, Pair<Class<? extends JsonDeserializer<Object>>, Class<?>>> DESERIALIZERS = ConcurrentHashMap.newMap();

    private ExtensionProvider() {}

    public static synchronized void registerExtensionDeserializer(final String name,
                                                                  final Class<? extends JsonDeserializer<Object>> deserializer,
                                                                  final Class<?> targetType) {
        DESERIALIZERS.put(name, Pair.of(deserializer, targetType));
    }

    public static synchronized Pair<Class<? extends JsonDeserializer<Object>>, Class<?>> getExtensionDeserializer(final String name) {
        return DESERIALIZERS.get(name);
    }

}
