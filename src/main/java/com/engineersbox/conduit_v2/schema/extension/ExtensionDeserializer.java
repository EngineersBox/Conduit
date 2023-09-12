package com.engineersbox.conduit_v2.schema.extension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

public class ExtensionDeserializer extends StdDeserializer<ImmutableMap<String, Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionDeserializer.class);
    private static final boolean ALLOW_ANONYMOUS_PRIMITIVE_DESERIALIZATION = true;

    protected ExtensionDeserializer() {
        super(ImmutableMap.class);
    }

    @Override
    public ImmutableMap<String, Object> deserialize(final JsonParser parser,
                                                    final DeserializationContext context) throws IOException {
        final MutableMap<String, Object> extensions = Maps.mutable.empty();
        final ObjectCodec codec = parser.getCodec();
        final JsonNode extensionsNode = codec.readTree(parser);
        if (nodeMissing(extensionsNode)) {
            throw new JsonParseException(
                    parser,
                    "Unable to parse extensions node "
            );
        }
        for (Iterator<Map.Entry<String, JsonNode>> it = extensionsNode.fields(); it.hasNext();) {
            final Map.Entry<String, JsonNode> field = it.next();
            final String name = field.getKey();
            final JsonNode node = field.getValue();
            final Pair<Class<? extends JsonDeserializer<?>>, TypeReference<?>> deserializerClass = ExtensionProvider.getExtensionDeserializer(name);
            if (deserializerClass == null) {
                if (ALLOW_ANONYMOUS_PRIMITIVE_DESERIALIZATION && !node.isContainerNode()) {
                    extensions.put(
                            name,
                            codec.readValue(
                                    node.traverse(codec),
                                    Object.class
                            )
                    );
                    continue;
                }
                throw new JsonParseException(parser, "No extension deserializer registered for name " + name);
            } else if (deserializerClass.getLeft().equals(JsonDeserializer.None.class)) {
                extensions.put(
                        name,
                        codec.readValue(
                                node.traverse(codec),
                                deserializerClass.getRight()
                        )
                );
                continue;
            }
            try {
                final JsonDeserializer<?> deserializer = instantiateDeserializer(
                        deserializerClass.getLeft(),
                        deserializerClass.getRight()
                );
                extensions.put(
                        name,
                        deserializer.deserialize(
                                field.getValue().traverse(codec),
                                context
                        )
                );
            } catch (final InvocationTargetException | InstantiationException | NoSuchMethodException | IllegalAccessException e) {
                throw new JsonParseException(parser, "Unable to instantiate deserializer for " + name);
            }
        }
        return extensions.toImmutable();
    }

    private boolean nodeMissing(final JsonNode node) {
        return node == null
                || node.isNull()
                || node.isMissingNode();
    }

    private JsonDeserializer<?> instantiateDeserializer(final Class<? extends JsonDeserializer<?>> deserializerClass,
                                                        final TypeReference<?> targetClass) throws InvocationTargetException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        try {
            return ConstructorUtils.invokeConstructor(deserializerClass);
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            LOGGER.trace(
                    "Failed to invoke default constructor for {}, attempting invocation of constructor accepting TypeReference<?>",
                    deserializerClass.getName()
            );
            return ConstructorUtils.invokeConstructor(
                    deserializerClass,
                    targetClass.getType()
            );
        }
    }

}
