package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;

public class UriDeserialiser extends JsonDeserializer<URI> {

    @Override
    public URI deserialize(final JsonParser p,
                           final DeserializationContext ctxt) throws IOException {
        final JsonNode node = p.getCodec().readTree(p);
        final String stringUrl = node.asText();
        return URI.create(stringUrl);
    }

}
