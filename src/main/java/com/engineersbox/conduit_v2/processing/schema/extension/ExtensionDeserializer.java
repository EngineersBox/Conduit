package com.engineersbox.conduit_v2.processing.schema.extension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

public class ExtensionDeserializer extends StdDeserializer<Object> {

    protected ExtensionDeserializer() {
        super(MutableMap.class);
    }

    @Override
    public Object deserialize(final JsonParser parser,
                              final DeserializationContext context) throws IOException {
//        final MutableMap<String, Object> extensions = Maps.mutable.empty();
        final String name = parser.getParsingContext().getCurrentName();
        final JsonNode extensionNode = parser.getCodec().readTree(parser);
        if (nodeMissing(extensionNode)) {
            return new JsonParseException(
                    parser,
                    "Unable to parse extension node " + name
            );
        }
        final Pair<Class<? extends JsonDeserializer<Object>>, Class<?>> deserializerClass = ExtensionProvider.getExtensionDeserializer(name);
        if (deserializerClass == null) {
            throw new JsonParseException(parser, "No extension deserializer registered for name " + name);
        }
        try {
            final JsonDeserializer<Object> deserializer = instantiateDeserializer(
                    deserializerClass.getLeft(),
                    deserializerClass.getRight()
            );
            return deserializer.deserialize(
                    parser,
                    context
            );
        } catch (final InvocationTargetException | InstantiationException | NoSuchMethodException | IllegalAccessException e) {
            throw new JsonParseException(parser, "Unable to instantiate deserializer for " + name);
        }
//        for (Iterator<Map.Entry<String, JsonNode>> it = extensionsNode.fields(); it.hasNext();) {
//            final Map.Entry<String, JsonNode> field = it.next();
//            final String name = field.getKey();
//            final Pair<Class<? extends JsonDeserializer<Object>>, Class<?>> deserializerClass = ExtensionProvider.getExtensionDeserializer(name);
//            if (deserializerClass == null) {
//                throw new JsonParseException(parser, "No extension deserializer registered for name " + name);
//            }
//            try {
//                final JsonDeserializer<Object> deserializer = instantiateDeserializer(
//                        deserializerClass.getLeft(),
//                        deserializerClass.getRight()
//                );
//                extensions.put(
//                        name,
//                        deserializer.deserialize(
//                                field.getValue().traverse(codec),
//                                context
//                        )
//                );
//            } catch (final InvocationTargetException | InstantiationException | NoSuchMethodException | IllegalAccessException e) {
//                throw new JsonParseException(parser, "Unable to instantiate deserializer for " + name);
//            }
//        }
//        return extensions;
    }

    private boolean nodeMissing(final JsonNode node) {
        return node == null
                || node.isNull()
                || node.isMissingNode();
    }

    private JsonDeserializer<Object> instantiateDeserializer(final Class<? extends JsonDeserializer<Object>> deserializerClass,
                                                             final Class<?> targetClass) throws InvocationTargetException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        try {
            return ConstructorUtils.invokeConstructor(deserializerClass);
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            return ConstructorUtils.invokeConstructor(
                    deserializerClass,
                    targetClass
            );
        }
    }

}
