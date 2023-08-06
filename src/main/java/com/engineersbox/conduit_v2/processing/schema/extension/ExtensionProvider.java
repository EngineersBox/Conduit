package com.engineersbox.conduit_v2.processing.schema.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.map.ConcurrentMutableMap;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionProvider.class);
    private static final ConcurrentMutableMap<String, Pair<Class<? extends JsonDeserializer<?>>, TypeReference<?>>> DESERIALIZERS = ConcurrentHashMap.newMap();
    private static final ConcurrentMutableMap<String, ExtensionSchemaPatch> SCHEMA_PATCHES = ConcurrentHashMap.newMap();

    private ExtensionProvider() {}

    public static synchronized <T extends Extension> void registerExtension(final T extension) {
        if (StringUtils.isBlank(extension.name())) {
            throw new IllegalArgumentException("Cannot register extension with blank name");
        }
        Class<? extends JsonDeserializer<?>> deserializer = extension.deserializerClass();
        final TypeReference<?> targetType = extension.targetType();
        if (targetType != null && ((Class<?>) targetType.getType()).isAnnotationPresent(JsonDeserialize.class)) {
            deserializer = JsonDeserializer.None.class;
        } else {
            LOGGER.trace("Target type was null or no @JsonDeserialize annotation present, defaulting to provided JsonDeserializer<?> implementation for \"{}\"", extension.name());
        }
        registerExtensionSchemaPatch(extension.name(), extension);
        registerExtensionDeserializer(
                extension.name(),
                deserializer,
                targetType
        );
    }

    public static synchronized void registerExtensionDeserializer(final String name,
                                                                  final Class<? extends JsonDeserializer<?>> deserializer,
                                                                  final TypeReference<?> targetType) {
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

    public static synchronized void registerExtensionSchemaPatch(final String name,
                                                                 final ExtensionSchemaPatch schemaPatch) {
        SCHEMA_PATCHES.put(name, schemaPatch);
    }

    public static synchronized Pair<Class<? extends JsonDeserializer<?>>, TypeReference<?>> getExtensionDeserializer(final String name) {
        return DESERIALIZERS.get(name);
    }

    public static synchronized ExtensionSchemaPatch getExtensionSchemaPatch(final String name) {
        return SCHEMA_PATCHES.get(name);
    }

    public static synchronized RichIterable<ExtensionSchemaPatch> getExtensionSchemaPatchesView() {
        return SCHEMA_PATCHES.valuesView();
    }

}
