package com.engineersbox.conduit.schemamerger.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@FunctionalInterface
public interface SchemaTransformer {

    ObjectNode transform(final ObjectMapper mapper, final ObjectNode resourceRef);

}
