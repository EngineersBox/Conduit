package com.engineersbox.conduit.schema.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.IllegalClassFormatException;
import java.util.Collection;

public class EnumRefResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumRefResolver.class);
    public static final String TOKEN = "enumRef";

    private EnumRefResolver() {
        throw new UnsupportedOperationException("Static utility class");
    }

    public static void resolve(final JsonNode rootNode) {
        JsonNode parentNode;
        while ((parentNode = rootNode.findParent(TOKEN)) != null) {
            final JsonNode enumRefNode = parentNode.get(TOKEN);
            LOGGER.trace(
                    "Found {} node ({}) in schema, affixing enum to parent node ({})",
                    TOKEN,
                    enumRefNode,
                    parentNode
            );
            affixEnum(
                    parentNode,
                    enumRefNode
            );
        }
    }

    private static void affixEnum(final JsonNode parentNode,
                                  final JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            throw new IllegalStateException("Expected object definition for enumRef of the form { \"class\": \"<class path>\", \"inclusionFieldName\": \"<field name>\", \"inclusionFieldValue\": \"<string field value>\"\"}");
        }
        final JsonNode classNameNode = node.get("class");
        if (classNameNode == null || classNameNode.isNull() || classNameNode.isMissingNode()) {
            throw new IllegalStateException(TOKEN + "$className: expected \"class\" field in enumRef definition");
        }
        final String className = classNameNode.asText();
        final JsonNode inclusionFieldNameNode = node.get("inclusionFieldName");
        if (inclusionFieldNameNode == null || inclusionFieldNameNode.isNull() || inclusionFieldNameNode.isMissingNode()) {
            throw new IllegalStateException(TOKEN + "$inclusionFieldName: expected \"inclusionFieldName\" field in enumRef definition");
        }
        final String inclusionFieldName = inclusionFieldNameNode.asText();
        final JsonNode inclusionFieldValueNode = node.get("inclusionFieldValue");
        if (inclusionFieldValueNode == null || inclusionFieldValueNode.isNull() || inclusionFieldValueNode.isMissingNode()) {
            throw new IllegalStateException(TOKEN + "$inclusionFieldValue: expected \"inclusionFieldValue\" field in enumRef definition");
        }
        final String inclusionFieldValue = inclusionFieldValueNode.asText();
        if (parentNode == null || parentNode.isMissingNode() || !(parentNode instanceof ObjectNode objectNode)) {
            throw new IllegalStateException("Expected a parent \"type\" node");
        }

        final ArrayNode enumArray = objectNode.putArray("enum");
        try {
            final StringBuilder logBuilder = new StringBuilder();
            enumArray.addAll(constructEnumNodes(
                    className,
                    inclusionFieldName,
                    inclusionFieldValue,
                    logBuilder
            ));
            LOGGER.trace(
                    "Constructed enum field on parent with values [{}]",
                    StringUtils.stripEnd(logBuilder.toString(), ",")
            );
        } catch (IllegalClassFormatException | ClassNotFoundException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        objectNode.remove(TOKEN);
        LOGGER.trace("Removed enumRef node from parent");
    }

    @SuppressWarnings({"unchecked"})
    private static Collection<? extends JsonNode> constructEnumNodes(final String className,
                                                                     final String fieldName,
                                                                     final String fieldValue,
                                                                     final StringBuilder logBuilder) throws ClassNotFoundException, IllegalClassFormatException, IllegalAccessException {
        final MutableList<JsonNode> elements = Lists.mutable.empty();
        final Class<?> clazz;
        if (StringUtils.isEmpty(className)) {
            throw new ClassNotFoundException("Class name must not be null or empty");
        }
        clazz = Class.forName(className);
        if (!clazz.isEnum()) {
            throw new IllegalClassFormatException("Class " + className + " is not an enum");
        }
        final Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;
        final Enum<?>[] enumConstants = enumClass.getEnumConstants();
        for (final Enum<?> enumConstant : enumConstants) {
            final String enumFieldValue = String.valueOf(FieldUtils.readField(enumConstant, fieldName, true));
            if (enumFieldValue.equals(fieldValue)) {
                final String name = enumConstant.name();
                logBuilder.append(name).append(",");
                elements.add(TextNode.valueOf(name));
            }
        }
        return elements;
    }

}
