package com.engineersbox.conduit.compile.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;

@FunctionalInterface
public interface SchemaTransformer {

    ObjectNode transform(final ObjectNode resourceRef);

}
