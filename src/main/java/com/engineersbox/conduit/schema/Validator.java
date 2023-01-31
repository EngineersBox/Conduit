package com.engineersbox.conduit.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;

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
                .getResourceAsStream("")) {
            node = OBJECT_MAPPER.readTree(resource);
        } catch (final IOException ignored) {
            // Won't occur
        }
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(node));
        SCHEMA = factory.getSchema(node);
    }

    public static Set<ValidationMessage> validate(final JsonNode metricsSchema) {
        return SCHEMA.validate(metricsSchema);
    }

}
