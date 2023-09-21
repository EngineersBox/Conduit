package com.engineersbox.conduit.core.schema.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.jayway.jsonpath.Configuration;

import java.io.IOException;

public class JsonPathConfigDeserializer extends StdDeserializer<Configuration> {

    protected JsonPathConfigDeserializer() {
        this(Configuration.class);
    }

    protected JsonPathConfigDeserializer(final Class<?> vc) {
        super(vc);
    }

    protected JsonPathConfigDeserializer(final JavaType valueType) {
        super(valueType);
    }

    protected JsonPathConfigDeserializer(final StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public Configuration deserialize(final JsonParser parser,
                                     final DeserializationContext _context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        return Configuration.builder()
                .jsonProvider(DataTypeProvider.JSON_PROVIDERS.get(node.get("json_provider").asText()).get())
                .mappingProvider(DataTypeProvider.MAPPING_PROVIDERS.get(node.get("mapping_provider").asText()).get())
                .build();
    }
}
