package com.engineersbox.conduit_v2.processing.schema.extension;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.internal.Utils;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class ExtensionProvider {
    private static final AtomicReferenceFieldUpdater<ExtensionProvider, Class<? extends JsonDeserializer<Object>>> UPDATER = AtomicReferenceFieldUpdater.newUpdater(
            ExtensionProvider.class,
            (Class<JsonDeserializer<Object>>) JsonDeserializer.None.class,
            "extensionDeserializer"
    );
    private static final ExtensionProvider instance = new ExtensionProvider();
    private volatile Class<? extends StdDeserializer<Object>> extensionDeserializer;

    public ExtensionProvider() {
    }

    public static void setExtensionProvider(final StdDeserializer<Object> deserializer) {
        Utils.notNull(deserializer, "Cache may not be null");
        if (!UPDATER.compareAndSet(instance, (Object)null, deserializer)) {
            throw new JsonPathException("Cache provider must be configured before deserializer is accessed and must not be registered twice.");
        }
    }

    public static Class<? extends JsonDeserializer<Object>> getDeserializer() {
        return ExtensionDeserializerHolder.DESERIALIZER;
    }

    private static Class<? extends JsonDeserializer<Object>> getDefaultCache() {
        return JsonDeserializer.None.class;
    }

    private static class ExtensionDeserializerHolder {
        static final Class<? extends JsonDeserializer<Object>> DESERIALIZER;

        private ExtensionDeserializerHolder() {
        }

        static {
            Class<? extends JsonDeserializer<Object>> deserializer = ExtensionProvider.instance.extensionDeserializer;
            if (deserializer == null) {
                deserializer = ExtensionProvider.getDefaultCache();
                if (!ExtensionProvider.UPDATER.compareAndSet(ExtensionProvider.instance, (Object)null, deserializer)) {
                    deserializer = ExtensionProvider.instance.extensionDeserializer;
                }
            }

            DESERIALIZER = deserializer;
        }
    }
}
