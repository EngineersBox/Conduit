package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.constraint;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.security.AlgorithmConstraints;

public class AlgorithmConstraintDeserializer extends StdDeserializer<AlgorithmConstraints> {

    protected AlgorithmConstraintDeserializer() {
        super(AlgorithmConstraints.class);
    }

    @Override
    public AlgorithmConstraints deserialize(final JsonParser p,
                                            final DeserializationContext ctxt) throws IOException {
        final JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        return AlgorithmConstraintsProvider.getConstraint(node.asText());
    }
}
