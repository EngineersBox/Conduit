package com.engineersbox.conduit.schemamerger.transformer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.instrument.IllegalClassFormatException;
import java.util.Collection;

public class EnumRefTransformer implements SchemaTransformer {

    private static class EnumRef {

        @JsonProperty(
                value ="class",
                required = true
        )
        @Nonnull
        public String className;
        @JsonProperty(value = "inclusionFieldName")
        @Nullable
        public String inclusionFieldName = null;
        @JsonProperty(value ="inclusionFieldValue")
        @Nullable
        public String inclusionFieldValue = null;

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumRefTransformer.class);
    public static final String TOKEN = "enumRef";

    private void affixEnum(final JsonNode parentNode,
                           final EnumRef enumRef) {
        if (parentNode == null || parentNode.isMissingNode() || !(parentNode instanceof ObjectNode objectNode)) {
            throw new IllegalStateException("Expected a parent \"type\" node");
        }

        final ArrayNode enumArray = objectNode.putArray("enum");
        try {
            final StringBuilder logBuilder = new StringBuilder();
            enumArray.addAll(constructEnumNodes(
                    enumRef,
                    logBuilder
            ));
            LOGGER.debug(
                    "Constructed enum field on parent with values [{}]",
                    StringUtils.stripEnd(logBuilder.toString(), ",")
            );
        } catch (IllegalClassFormatException | ClassNotFoundException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        objectNode.remove(TOKEN);
        LOGGER.debug("Removed enumRef node from parent");
    }

    @SuppressWarnings({"unchecked"})
    private Collection<? extends JsonNode> constructEnumNodes(final EnumRef enumRef,
                                                              final StringBuilder logBuilder) throws ClassNotFoundException, IllegalClassFormatException, IllegalAccessException {
        final MutableList<JsonNode> elements = Lists.mutable.empty();
        final Class<?> clazz;
        if (StringUtils.isEmpty(enumRef.className)) {
            throw new ClassNotFoundException("Class name must not be null or empty");
        }
        clazz = Class.forName(enumRef.className);
        if (!clazz.isEnum()) {
            throw new IllegalClassFormatException("Class " + enumRef.className + " is not an enum");
        }
        final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;
        final Enum<?>[] enumConstants = enumClass.getEnumConstants();
        for (final Enum<?> enumConstant : enumConstants) {
            if (enumRef.inclusionFieldName == null) {
                addElement(enumConstant, elements, logBuilder);
                continue;
            }
            final String enumFieldValue = String.valueOf(FieldUtils.readField(
                    enumConstant,
                    enumRef.inclusionFieldName,
                    true
            ));
            if (enumFieldValue.equals(enumRef.inclusionFieldValue)) {
                addElement(enumConstant, elements, logBuilder);
            }
        }
        return elements;
    }

    private void addElement(final Enum<?> enumConstant,
                            final MutableList<JsonNode> elements,
                            final StringBuilder logBuilder) {
        final String name = enumConstant.name();
        logBuilder.append(name).append(",");
        elements.add(TextNode.valueOf(name));
    }

    @Override
    public ObjectNode transform(final ObjectMapper mapper, final ObjectNode schema) {
        JsonNode parentNode;
        while ((parentNode = schema.findParent(TOKEN)) != null) {
            final JsonNode enumRefNode = parentNode.get(TOKEN);
            LOGGER.debug(
                    "Found {} node ({}) in schema, affixing enum to parent node ({})",
                    TOKEN,
                    enumRefNode,
                    parentNode
            );
            try {
                final EnumRef enumRef = mapper.treeToValue(enumRefNode, EnumRef.class);
                affixEnum(
                        parentNode,
                        enumRef
                );
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return schema;
    }
}
