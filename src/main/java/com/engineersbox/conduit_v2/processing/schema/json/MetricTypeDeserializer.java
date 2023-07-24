package com.engineersbox.conduit_v2.processing.schema.json;

import com.engineersbox.conduit.schema.DimensionallyIndexedRangeMap;
import com.engineersbox.conduit_v2.processing.schema.MetricKind;
import com.engineersbox.conduit_v2.processing.schema.MetricType;
import com.engineersbox.conduit_v2.processing.schema.ParameterizedMetricType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class MetricTypeDeserializer extends JsonDeserializer<MetricType> {

    @Override
    public MetricType deserialize(final JsonParser parser,
                                  final DeserializationContext _context) throws IOException {
        final ObjectCodec codec = parser.getCodec();
        final JsonNode node = codec.readTree(parser);
        final MetricKind type = MetricKind.valueOf(get(node, "type").asText());
        final DimensionallyIndexedRangeMap suffixes = null;
        final JsonNode structureNode = node.get("structure");
        final ParameterizedMetricType structure = nodeMissing(structureNode) ? null : codec.treeToValue(structureNode, ParameterizedMetricType.class);
        return new MetricType(
                type,
                structure,
                suffixes
        );
    }

    private JsonNode get(final JsonNode parent,
                         final String childName) throws JsonMappingException {
        final JsonNode child = parent.get(childName);
        if (nodeMissing(child)) {
            throw new JsonMappingException(
                    parent.traverse(),
                    String.format(
                            "Expected \"%s\" node in metric structure declaration, found none",
                            childName
                    )
            );
        }
        return child;
    }

    private boolean nodeMissing(final JsonNode node) {
        return node == null || node.isEmpty() || node.isNull() || node.isMissingNode();
    }

}
