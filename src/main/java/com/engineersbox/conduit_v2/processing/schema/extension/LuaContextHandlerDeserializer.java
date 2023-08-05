package com.engineersbox.conduit_v2.processing.schema.extension;

import com.engineersbox.conduit.handler.LuaContextHandler;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.nio.file.Path;

public class LuaContextHandlerDeserializer extends StdDeserializer<LuaContextHandler> {

    public LuaContextHandlerDeserializer() {
        super(LuaContextHandler.class);
    }

    @Override
    public LuaContextHandler deserialize(final JsonParser parser,
                                         final DeserializationContext _context) throws IOException, JacksonException {
        final JsonNode pathNode = parser.getCodec().readTree(parser);
        if (pathNode == null) {
            return null;
        }
        return new LuaContextHandler(
                Path.of(pathNode.asText())
                        .toAbsolutePath()
                        .toString(),
                LuaHandlerExtension.GLOBALS_PROVIDER
        );
    }
}
