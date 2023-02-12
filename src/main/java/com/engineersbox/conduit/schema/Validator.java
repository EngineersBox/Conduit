package com.engineersbox.conduit.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class Validator {

    private static final JsonSchema SCHEMA;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        JsonNode node = null;
        try (final InputStream resource = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("schemas/metrics.json")) {
            node = OBJECT_MAPPER.readTree(resource);
        } catch (final IOException ignored) {
            // Won't occur
        }
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(node));
        SCHEMA = factory.getSchema(node);
        SCHEMA.initializeValidators();
    }

    public static Set<ValidationMessage> validate(final JsonNode metricsSchema) {
        return SCHEMA.validate(metricsSchema);
    }

}
