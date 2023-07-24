package com.engineersbox.conduit_v2.processing.schema.json;

import com.engineersbox.conduit.schema.DimensionallyIndexedRangeMap;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class DimensionallyIndexedRangeMapDeserializer extends JsonDeserializer<DimensionallyIndexedRangeMap> {

    @Override
    public DimensionallyIndexedRangeMap deserialize(final JsonParser parser,
                                                    final DeserializationContext _context) throws IOException, JacksonException {
        return null;
    }

}
