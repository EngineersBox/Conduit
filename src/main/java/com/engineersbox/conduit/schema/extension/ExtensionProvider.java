package com.engineersbox.conduit.schema.extension;

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

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ExtensionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionProvider.class);
    private static final ConcurrentMutableMap<String, Pair<Class<? extends JsonDeserializer<?>>, TypeReference<?>>> DESERIALIZERS = ConcurrentHashMap.newMap();
    private static final ConcurrentMutableMap<String, ExtensionMetadata> SCHEMA_METADATA = ConcurrentHashMap.newMap();

    private ExtensionProvider() {}

    public static <T extends ExtensionMetadata> void registerExtension(final T extension) {
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
        registerExtensionMetadata(extension.name(), extension);
        registerExtensionDeserializer(
                extension.name(),
                deserializer,
                targetType
        );
    }

    public static void registerExtensionDeserializer(final String name,
                                                                  final Class<? extends JsonDeserializer<?>> deserializer,
                                                                  final TypeReference<?> targetType) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Cannot register extension deserializer with blank name");
        }
        synchronized (DESERIALIZERS) {
            DESERIALIZERS.put(
                    name,
                    Pair.of(
                            deserializer,
                            targetType
                    )
            );
        }
    }

    public static void registerExtensionMetadata(final String name,
                                                              final ExtensionMetadata metadata) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Cannot register extension metadata with blank name");
        }
        synchronized (SCHEMA_METADATA) {
            SCHEMA_METADATA.put(name, metadata);
        }
    }

    public static Pair<Class<? extends JsonDeserializer<?>>, TypeReference<?>> getExtensionDeserializer(final String name) {
        synchronized (DESERIALIZERS) {
            return DESERIALIZERS.get(name);
        }
    }

    public static ExtensionMetadata getExtensionMetadata(final String name) {
        synchronized (SCHEMA_METADATA) {
            return SCHEMA_METADATA.get(name);
        }
    }

    public static RichIterable<ExtensionMetadata> getExtensionMetadataView() {
        synchronized (SCHEMA_METADATA) {
            return SCHEMA_METADATA.valuesView();
        }
    }

}
