package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;

public class UriDeserialiser extends JsonDeserializer<URI> {

    @Override
    public URI deserialize(final JsonParser parser,
                           final DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        if (node == null
            || node.isNull()
            || node.isMissingNode()) {
            throw new JsonParseException(parser, "Expected 'uri' field to have a value");
        }
        final String stringUrl = node.asText();
        return URI.create(stringUrl);
    }

}
