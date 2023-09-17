package com.engineersbox.conduit.schema;

import com.engineersbox.conduit.util.ObjectMapperModule;
import com.engineersbox.conduit.schema.extension.ExtensionMetadata;
import com.engineersbox.conduit.schema.extension.ExtensionProvider;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.networknt.schema.*;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

public class Validator {

    private static final String SCHEMA_PATCH_TEMPLATE = """
            [
                {
                    "op": "add",
                    "path": "/properties/extensions/properties/%s",
                    "value": %s
                }
            ]
            """;
    private static final JsonSchema SCHEMA;

    static {
        JsonNode node;
        try (final InputStream resource = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("schemas/metrics.schema.json")) {
            node = ObjectMapperModule.OBJECT_MAPPER.readTree(resource);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        for (final ExtensionMetadata extensionMetadata : ExtensionProvider.getExtensionMetadataView()) {
            try {
                node = applySchemaPatch(extensionMetadata, node);
            } catch (final IOException | JsonPatchException e) {
                throw new IllegalStateException(e);
            }
        }
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(node));
        final SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        applyValidatorConfig(config);
        SCHEMA = factory.getSchema(node, config);
        SCHEMA.initializeValidators();
    }

    private static void applyValidatorConfig(final SchemaValidatorsConfig config) {
        config.addKeywordWalkListener("enum", new JsonSchemaWalkListener() {
            @Override
            public WalkFlow onWalkStart(final WalkEvent walkEvent) {
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(final WalkEvent walkEvent,
                                  final Set<ValidationMessage> set) {
                final JsonNode node = walkEvent.getSchemaNode().get("enum");
                if (node == null) {
                    return;
                } else if (node.isNull() || node.isMissingNode()) {
                    set.add(ValidationMessage.of(
                            "enumRef",
                            createMessage("Expected object definition for enumRef of the form { \"class\": \"<class path>\", \"inclusionFieldName\": \"<field name>\", \"inclusionFieldValue\": \"<string field value>\"\"}"),
                            new MessageFormat("Invalid enumRef definition"),
                            walkEvent.getAt(),
                            walkEvent.getSchemaPath()
                    ));
                    return;
                }
                final JsonNode classNameNode = node.get("class");
                if (classNameNode == null || classNameNode.isNull() || classNameNode.isMissingNode()) {
                    set.add(createValidationMessage(
                            "enumRef$className",
                            "expected \"class\" field in enumRef definition",
                            classNameNode == null ? node : classNameNode,
                            walkEvent
                    ));
                    return;
                }
                final String className = classNameNode.asText();
                final JsonNode inclusionFieldNameNode = node.get("inclusionFieldName");
                if (inclusionFieldNameNode == null || inclusionFieldNameNode.isNull() || inclusionFieldNameNode.isMissingNode()) {
                    set.add(createValidationMessage(
                            "enumRef$inclusionFieldName",
                            "expected \"inclusionFieldName\" field in enumRef definition",
                            inclusionFieldNameNode == null ? node : inclusionFieldNameNode,
                            walkEvent
                    ));
                    return;
                }
                final String inclusionFieldName = inclusionFieldNameNode.asText();
                final JsonNode inclusionFieldValueNode = node.get("inclusionFieldValue");
                if (inclusionFieldValueNode == null || inclusionFieldValueNode.isNull() || inclusionFieldValueNode.isMissingNode()) {
                    set.add(createValidationMessage(
                            "enumRef$inclusionFieldValue",
                            "expected \"inclusionFieldValue\" field in enumRef definition",
                            inclusionFieldValueNode == null ? node : inclusionFieldValueNode,
                            walkEvent
                    ));
                    return;
                }
                final String inclusionFieldValue = inclusionFieldValueNode.asText();
                final JsonNode typeNode = node.findParent("type");
                if (typeNode == null || typeNode.isMissingNode() || !(typeNode instanceof ObjectNode objectNode)) {
                    return;
                }

                final ArrayNode enumArray = objectNode.putArray("enum");
                try {
                    enumArray.addAll(constructEnumNodes(
                            className,
                            inclusionFieldName,
                            inclusionFieldValue
                    ));
                } catch (IllegalClassFormatException | ClassNotFoundException | IllegalAccessException e) {
                    set.add(createValidationMessage(
                            "enumRef",
                            e.getMessage(),
                            node,
                            walkEvent
                    ));
                }
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    private static Collection<? extends JsonNode> constructEnumNodes(final String className,
                                                                     final String fieldName,
                                                                     final String fieldValue) throws ClassNotFoundException, IllegalClassFormatException, IllegalAccessException {
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
                elements.add(TextNode.valueOf(enumConstant.name()));
            }
        }
        return elements;
    }

    private static ValidationMessage createValidationMessage(final String type,
                                                             final String message,
                                                             final JsonNode node,
                                                             final WalkEvent event) {
        try (final JsonParser parser = node.traverse()) {
            return ValidationMessage.of(
                    type,
                    createMessage(message),
                    new MessageFormat(message),
                    parser.getCurrentLocation().toString(),
                    event.getSchemaPath()
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ErrorMessageType createMessage(final String message) {
        return new ErrorMessageType() {
            @Override
            public String getCustomMessage() {
                return message;
            }

            @Override
            public String getErrorCodeValue() {
                return "-1";
            }

            @Override
            public String getErrorCode() {
                return "-1";
            }
        };
    }

    private static JsonNode applySchemaPatch(final ExtensionMetadata extensionMetadata,
                                             final JsonNode schemaNode) throws IOException, JsonPatchException {
        final String literal = extensionMetadata.schemaLiteral();
        if (StringUtils.isNotBlank(literal)) {
            final String schemaPatch = String.format(
                    SCHEMA_PATCH_TEMPLATE,
                    extensionMetadata.name(),
                    literal
            );
            final JsonPatch patch = ObjectMapperModule.OBJECT_MAPPER.readValue(schemaPatch, JsonPatch.class);
            return patch.apply(schemaNode);
        }
        final InputStream stream = extensionMetadata.schemaStream();
        if (stream != null) {
            final String schemaPatch = String.format(
                    SCHEMA_PATCH_TEMPLATE,
                    extensionMetadata.name(),
                    new String(stream.readAllBytes(), StandardCharsets.UTF_8)
            );
            stream.close();
            final JsonPatch patch = ObjectMapperModule.OBJECT_MAPPER.readValue(schemaPatch, JsonPatch.class);
            return patch.apply(schemaNode);
        }
        final String literalPatch = extensionMetadata.schemaPatchLiteral();
        if (StringUtils.isNotBlank(literalPatch)) {
            final JsonPatch patch = ObjectMapperModule.OBJECT_MAPPER.readValue(literalPatch, JsonPatch.class);
            return patch.apply(schemaNode);
        }
        final InputStream streamPatch = extensionMetadata.schemaPatchStream();
        if (streamPatch != null) {
            final JsonPatch patch = ObjectMapperModule.OBJECT_MAPPER.readValue(streamPatch, JsonPatch.class);
            streamPatch.close();
            return patch.apply(schemaNode);
        }
        return schemaNode;
    }

    public static Set<ValidationMessage> validate(final JsonNode metricsSchema) {
        return SCHEMA.validate(metricsSchema);
    }

}
