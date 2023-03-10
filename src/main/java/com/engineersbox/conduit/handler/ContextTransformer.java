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

    private final Map<String, LuaTable> directTables;
    private final Map<String, Pair<Object, Class<? extends JsonSerializer<?>>>> readOnlyAttributes;

    private ContextTransformer() {
        this.directTables = new HashMap<>();
        this.readOnlyAttributes = new HashMap<>();
    }

    public static ContextTransformer builder() {
        return new ContextTransformer();
    }

    // Use default serializer
//    public ContextTransformer withReadOnly(final Object ...objects) {
//        for (final Object obj : objects) {
//            final Class<?> objClass = obj.getClass();
//            final LuaContextName ctxName = objClass.getAnnotation(LuaContextName.class);
//            final String name = ctxName == null ? objClass.getSimpleName() : ctxName.value();
//            withReadOnly(name, obj, StdSerializer.None.class);
//        }
//        return this;
//    }

    public ContextTransformer withReadOnly(final String name,
                                           final Object object) {
        return withReadOnly(name, object, null);
    }

    // Provide explicit serializer
    public ContextTransformer withReadOnly(final Object object,
                                           final Class<? extends JsonSerializer<?>> serializer) {
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

    public ContextTransformer withReadOnly(final String name,
                                           final Object object,
                                           final Class<? extends JsonSerializer<?>> serializer) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        } else if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        this.readOnlyAttributes.put(
                name,
                ImmutablePair.of(
                        object,
                        serializer
                )
        );
        return this;
    }

    public ContextTransformer withTable(final String name,
                                        final LuaTable table) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        } else if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        this.directTables.put(name, table);
        return this;
    }

    @SuppressWarnings("unchecked")
    public LuaTable transform() {
        final List<LuaValue> keysAndValues = new ArrayList<>();
        // Regular tables
        for (final Map.Entry<String, LuaTable> table : this.directTables.entrySet()) {
            keysAndValues.add(LuaString.valueOf(table.getKey()));
            keysAndValues.add(table.getValue());
        }
        // Read only attributes
        for (final Map.Entry<String, Pair<Object, Class<? extends JsonSerializer<?>>>> entry : this.readOnlyAttributes.entrySet()) {
            keysAndValues.add(LuaString.valueOf(entry.getKey()));
            final Pair<Object, Class<? extends JsonSerializer<?>>> pair = entry.getValue();
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
            keysAndValues.add(node.isValueNode() ? parseValue(node) : constructTable(node));
        }
        return LuaTable.tableOf(
                keysAndValues.toArray(LuaValue[]::new)
        );
    }

    private LuaTable constructTable(final JsonNode node) {
        final List<LuaValue> keysAndValues = new ArrayList<>();
        for (final Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            final Map.Entry<String, JsonNode> element = it.next();
            keysAndValues.add(LuaString.valueOf(element.getKey()));
            keysAndValues.add(parseValue(element.getValue()));
        }
        return LuaTable.tableOf(
                keysAndValues.toArray(LuaValue[]::new)
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
        } else if (node.isFloatingPointNumber()) {
            return LuaDouble.valueOf(node.asDouble());
        } else if (node.isIntegralNumber()) {
            return LuaInteger.valueOf(node.asInt());
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
