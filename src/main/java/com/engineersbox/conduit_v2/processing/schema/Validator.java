package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.util.ObjectMapperModule;
import com.engineersbox.conduit_v2.processing.schema.extension.ExtensionProvider;
import com.engineersbox.conduit_v2.processing.schema.extension.ExtensionSchemaPatch;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.networknt.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class Validator {

    private static final JsonSchema SCHEMA;

    static {
        JsonNode node;
        try (final InputStream resource = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("schemas/metrics.json")) {
            node = ObjectMapperModule.OBJECT_MAPPER.readTree(resource);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        for (final ExtensionSchemaPatch patch : ExtensionProvider.getExtensionSchemaPatchView()) {
            try {
                node = applySchemaPatch(patch, node);
            } catch (final IOException | JsonPatchException e) {
                throw new IllegalStateException(e);
            }
        }
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(node));
        final SchemaValidatorsConfig config = new SchemaValidatorsConfig();
//        config.setFailFast(true);
        SCHEMA = factory.getSchema(node, config);
        SCHEMA.initializeValidators();
    }

    private static JsonNode applySchemaPatch(final ExtensionSchemaPatch extensionSchemaPatch,
                                             final JsonNode schemaNode) throws IOException, JsonPatchException {
        final String literalPatch = extensionSchemaPatch.schemaPatchLiteral();
        if (StringUtils.isNotBlank(literalPatch)) {
            final JsonPatch patch = ObjectMapperModule.OBJECT_MAPPER.readValue(literalPatch, JsonPatch.class);
            return patch.apply(schemaNode);
        }
        final InputStream streamPatch = extensionSchemaPatch.schemaPatchStream();
        if (streamPatch != null) {
            final JsonPatch patch = ObjectMapperModule.OBJECT_MAPPER.readValue(streamPatch, JsonPatch.class);
            return patch.apply(schemaNode);
        }
        return schemaNode;
    }

    public static Set<ValidationMessage> validate(final JsonNode metricsSchema) {
        return SCHEMA.validate(metricsSchema);
    }

}
