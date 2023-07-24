package com.engineersbox.conduit_v2.processing.schema.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Range;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;

public class SuffixFormatDeserializer extends JsonDeserializer<Pair<Range<Integer>, String>> {

    @Override
    public Pair<Range<Integer>, String> deserialize(final JsonParser parser,
                                                    final DeserializationContext _context) throws IOException, JacksonException {
        final JsonNode suffixFormatNode = parser.getCodec().readTree(parser);
        if (nodeMissing(suffixFormatNode)) {
            return ImmutablePair.of(Range.all(), "/{name}{index}");
        }
        final JsonNode fromJsonNode = suffixFormatNode.get("from");
        final JsonNode toJsonNode = suffixFormatNode.get("to");
        final int from = nodeMissing(fromJsonNode) ? -1 : fromJsonNode.asInt();
        final int to = nodeMissing(toJsonNode) ? -1 : toJsonNode.asInt();
        Range<Integer> range;
        if (from == -1 && to == -1) {
            range = Range.all();
        } else if (from > -1 && to == -1) {
            range = Range.atLeast(from);
        } else if (from == -1 && to > -1) {
            range = Range.lessThan(to);
        } else {
            range = Range.closedOpen(from, to);
        }
        return ImmutablePair.of(
                range,
                suffixFormatNode.get("template").asText()
        );
    }

    private boolean nodeMissing(final JsonNode node) {
        return node == null
                || node.isNull()
                || node.isMissingNode();
    }

}
