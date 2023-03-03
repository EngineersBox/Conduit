package com.engineersbox.conduit.handler;

import com.engineersbox.conduit.handler.annotation.LuaContextName;
import com.engineersbox.conduit.util.ObjectMapperModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.luaj.vm2.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ContextTransformer {

    private final Map<String, Pair<Object, Class<? extends StdSerializer<?>>>> readOnlyAttributes;

    private ContextTransformer() {
        this.readOnlyAttributes = new HashMap<>();
    }

    public static ContextTransformer builder() {
        return new ContextTransformer();
    }

    // Use default serializer
    public ContextTransformer withReadOnly(final Object ...objects) {
        for (final Object obj : objects) {
            final Class<?> objClass = obj.getClass();
            final LuaContextName ctxName = objClass.getAnnotation(LuaContextName.class);
            final String name = ctxName == null ? objClass.getSimpleName() : ctxName.value();
            withReadOnly(obj, StdSerializer.None.class);
        }
        return this;
    }

    // Provide explicit serializer
    public ContextTransformer withReadOnly(final Object object,
                                           final Class<? extends StdSerializer<?>> serializer) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        final Class<?> objClass = object.getClass();
        final LuaContextName ctxName = objClass.getAnnotation(LuaContextName.class);
        final String name = ctxName == null ? objClass.getSimpleName() : ctxName.value();
        this.readOnlyAttributes.put(
                name,
                ImmutablePair.of(
                        object,
                        serializer
                )
        );
        return this;
    }

    @SuppressWarnings("unchecked")
    public LuaTable transform() {
        final List<LuaValue> keys = new ArrayList<>();
        final List<LuaValue> values = new ArrayList<>();
        for (final Map.Entry<String, Pair<Object, Class<? extends StdSerializer<?>>>> entry : this.readOnlyAttributes.entrySet()) {
            keys.add(LuaString.valueOf(entry.getKey()));
            final Pair<Object, Class<? extends StdSerializer<?>>> pair = entry.getValue();
            if (pair.getRight() != null) {
                final SimpleModule module = new SimpleModule();
                try {
                    module.addSerializer(
                            pair.getLeft().getClass(),
                            (JsonSerializer<Object>) pair.getRight().getConstructor().newInstance()
                    );
                } catch (final InvocationTargetException
                               | InstantiationException
                               | IllegalAccessException
                               | NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
                ObjectMapperModule.OBJECT_MAPPER.registerModule(module);
            }
            final JsonNode node = ObjectMapperModule.OBJECT_MAPPER.valueToTree(pair.getLeft());
            values.add(constructTable(node));
        }
        return LuaTable.tableOf(
                keys.toArray(LuaValue[]::new),
                values.toArray(LuaValue[]::new)
        );
    }

    private LuaTable constructTable(final JsonNode node) {
        final List<LuaValue> keys = new ArrayList<>();
        final List<LuaValue> values = new ArrayList<>();
        for (final Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            final Map.Entry<String, JsonNode> element = it.next();
            keys.add(LuaString.valueOf(element.getKey()));
            values.add(parseValue(element.getValue()));
        }
        return LuaTable.tableOf(
                keys.toArray(LuaValue[]::new),
                values.toArray(LuaValue[]::new)
        );
    }

    private LuaValue parseValue(final JsonNode node) {
        if (node.isObject()) {
            return constructTable(node);
        } else if (node.isArray()) {
            final LuaValue[] values = new LuaValue[node.size()];
            int index = 0;
            for (final JsonNode arrayNode : node) {
                values[index++] = parseValue(arrayNode);
            }
            return LuaTable.listOf(values);
        } else if (node.isNumber()) {
            return LuaNumber.valueOf(node.asText());
        } else if (node.isBoolean()) {
            return LuaBoolean.valueOf(node.asBoolean());
        } else if (node.isNull()) {
            return null;
        } else if (node.isTextual()) {
            return LuaString.valueOf(node.asText());
        }
        throw new IllegalArgumentException(String.format(
                "Cannot convert node of type %s into Lua equivalent",
                node.getNodeType()
        ));
    }

}
