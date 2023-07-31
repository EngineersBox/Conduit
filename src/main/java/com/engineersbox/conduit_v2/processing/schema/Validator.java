package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.util.ObjectMapperModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class Validator {

    private static final JsonSchema SCHEMA;

    static {
        JsonNode node = null;
        try (final InputStream resource = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("schemas/metrics.json")) {
            node = ObjectMapperModule.OBJECT_MAPPER.readTree(resource);
        } catch (final IOException ignored) {
            // Won't occur
        }
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(node));
        final SchemaValidatorsConfig config = new SchemaValidatorsConfig();
//        config.setFailFast(true);
        SCHEMA = factory.getSchema(node, config);
        SCHEMA.initializeValidators();
    }

    public static Set<ValidationMessage> validate(final JsonNode metricsSchema) {
        return SCHEMA.validate(metricsSchema);
    }

}
