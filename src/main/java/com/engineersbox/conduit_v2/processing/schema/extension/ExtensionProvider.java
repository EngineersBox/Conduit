package com.engineersbox.conduit_v2.processing.schema.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

public class ExtensionProvider {

    private static final ConcurrentMutableMap<String, Pair<Class<? extends JsonDeserializer<? extends Extension>>, TypeReference<? extends Extension>>> DESERIALIZERS = ConcurrentHashMap.newMap();

    private ExtensionProvider() {}

    @SuppressWarnings("unchecked")
    public static synchronized <T extends Extension> void registerExtension(final T extension) {
        if (StringUtils.isBlank(extension.name())) {
            throw new IllegalArgumentException("Cannot register extension with blank name");
        }
        final boolean hasDeserializeAnnotation = extension.getClass().isAnnotationPresent(JsonDeserialize.class);
        Class<? extends JsonDeserializer<? extends Extension>> deserializer = extension.deserializerClass();
        TypeReference<? extends Extension> targetType = extension.targetType();
        if (hasDeserializeAnnotation) {
            deserializer = (Class<? extends JsonDeserializer<? extends Extension>>) JsonDeserializer.None.class;
            if (targetType == null) {
                targetType = new TypeReference<T>() {};
            }
        }
        DESERIALIZERS.put(
                extension.name(),
                Pair.of(
                        deserializer,
                        targetType
                )
        );
    }

    public static synchronized <T extends Extension> void registerExtensionDeserializer(final String name,
                                                                                        final Class<? extends JsonDeserializer<T>> deserializer,
                                                                                        final TypeReference<T> targetType) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Cannot register extension with blank name");
        }
        DESERIALIZERS.put(
                name,
                Pair.of(
                        deserializer,
                        targetType
                )
        );
    }

    public static synchronized Pair<Class<? extends JsonDeserializer<? extends Extension>>, TypeReference<? extends Extension>> getExtensionDeserializer(final String name) {
        return DESERIALIZERS.get(name);
    }

}
