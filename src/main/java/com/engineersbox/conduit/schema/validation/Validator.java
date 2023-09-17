package com.engineersbox.conduit.schema.validation;

import com.engineersbox.conduit.schema.extension.ExtensionMetadata;
import com.engineersbox.conduit.schema.extension.ExtensionProvider;
import com.engineersbox.conduit.util.ObjectMapperModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.networknt.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

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
        EnumRefResolver.resolve(node);
        for (final ExtensionMetadata extensionMetadata : ExtensionProvider.getExtensionMetadataView()) {
            try {
                node = applySchemaPatch(extensionMetadata, node);
            } catch (final IOException | JsonPatchException e) {
                throw new IllegalStateException(e);
            }
        }
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(node));
        final SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        SCHEMA = factory.getSchema(node, config);
        SCHEMA.initializeValidators();
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
