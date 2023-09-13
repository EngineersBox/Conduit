package com.engineersbox.conduit.schema.extension.handler;

import com.engineersbox.conduit.schema.extension.handler.annotation.LuaContextName;
import com.engineersbox.conduit.util.ObjectMapperModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.luaj.vm2.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ContextTransformer {

    private final Map<String, LuaTable> directTables;
    private final Map<String, Pair<Object, Class<? extends JsonSerializer<?>>>> readOnlyAttributes;

    public ContextTransformer() {
        this.directTables = new HashMap<>();
        this.readOnlyAttributes = new HashMap<>();
    }

    public static ContextTransformer.Builder builder(final ContextTransformer transformer) {
        return new Builder(transformer);
    }

    @SuppressWarnings("unchecked")
    public LuaTable transform() {
        final List<LuaValue> keysAndValues = new ArrayList<>();
        // Regular tables
        this.directTables.forEach((final String key, final LuaTable value) -> {
            keysAndValues.add(LuaString.valueOf(key));
            keysAndValues.add(value);
        });
        // Read only attributes
        this.readOnlyAttributes.forEach((final String key, final Pair<Object, Class<? extends JsonSerializer<?>>> pair) -> {
            keysAndValues.add(LuaString.valueOf(key));
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
        });
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

    public static class Builder {

        private final ContextTransformer transformer;

        private Builder(final ContextTransformer transformer) {
            this.transformer = transformer;
        }

        public Builder withReadOnly(final String name,
                                    final Object object) {
            return withReadOnly(name, object, null);
        }

        // Provide explicit serializer
        public Builder withReadOnly(final Object object,
                                    final Class<? extends JsonSerializer<?>> serializer) {
            if (object == null) {
                throw new IllegalArgumentException("Object cannot be null");
            }
            final Class<?> objClass = object.getClass();
            final LuaContextName ctxName = objClass.getAnnotation(LuaContextName.class);
            final String name = ctxName == null ? objClass.getSimpleName() : ctxName.value();
            this.transformer.readOnlyAttributes.put(
                    name,
                    ImmutablePair.of(
                            object,
                            serializer
                    )
            );
            return this;
        }

        public Builder withReadOnly(final String name,
                                    final Object object,
                                    final Class<? extends JsonSerializer<?>> serializer) {
            if (name == null) {
                throw new IllegalArgumentException("Name cannot be null");
            } else if (object == null) {
                throw new IllegalArgumentException("Object cannot be null");
            }
            this.transformer.readOnlyAttributes.put(
                    name,
                    ImmutablePair.of(
                            object,
                            serializer
                    )
            );
            return this;
        }

        public Builder withTable(final String name,
                                 final LuaTable table) {
            if (name == null) {
                throw new IllegalArgumentException("Name cannot be null");
            } else if (table == null) {
                throw new IllegalArgumentException("Table cannot be null");
            }
            this.transformer.directTables.put(name, table);
            return this;
        }

        public ContextTransformer build() {
            return this.transformer;
        }

    }

}
