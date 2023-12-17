package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.proxy;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;

public class ProxySelectorDeserialiser extends JsonDeserializer<ProxySelector> {
    @Override
    public ProxySelector deserialize(final JsonParser parser,
                                     final DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull() || node.isEmpty()) {
            return ProxySelector.getDefault();
        } else if (node.isObject()) {
            final InetSocketAddress socketAddress = deseraliseInetSocketAddress(parser, node);
            return ProxySelector.of(socketAddress);
        }
        return null;
    }

    private InetSocketAddress deseraliseInetSocketAddress(final JsonParser parser,
                                                          final JsonNode node) throws JsonParseException {
        final JsonNode hostNode = node.get("host");
        if (hostNode == null || hostNode.isNull() || hostNode.isEmpty() || !hostNode.isTextual()) {
            throw new JsonParseException(parser, "Expected field 'host' to be present and of textual type");
        }
        final JsonNode portNode = node.get("port");
        if (portNode == null || portNode.isNull() || portNode.isEmpty() || !portNode.isInt()) {
            throw new JsonParseException(parser, "Expected field 'port' to be present and of integral type");
        }

        return InetSocketAddress.createUnresolved(
                hostNode.asText(),
                portNode.asInt()
        );
    }

}
