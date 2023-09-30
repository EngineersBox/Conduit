package com.engineersbox.conduit.core.retrieval.ingest.connection;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Supplier;

public class AnonymousConnectorDeserializer extends StdDeserializer<Connector<Object, ConnectorConfiguration>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnonymousConnectorDeserializer.class);

    public AnonymousConnectorDeserializer() {
        super(Connector.class);
    }

    @Override
    public Connector<Object, ConnectorConfiguration> deserialize(final JsonParser parser,
                                                                 final DeserializationContext ctxt) throws IOException, JacksonException {
        final Object attribute = ctxt.getAttribute(ConnectorTypeResolver.CURRENT_SUPPLIER_KEY);
        if (attribute == null) {
            throw new JsonParseException(
                    parser,
                    String.format(
                            "Expected supplier bound to %s attribute, found none",
                            ConnectorTypeResolver.CURRENT_SUPPLIER_KEY
                    )
            );
        } else if (!(attribute instanceof Supplier<?>)) {
            throw new JsonParseException(
                    parser,
                    String.format(
                            "Supplier bound to %s attribute is not instance of %s, instead %s",
                            ConnectorTypeResolver.CURRENT_SUPPLIER_KEY,
                            new TypeReference<Supplier<? extends Connector<?,?>>>(){}.getType().getTypeName(),
                            attribute.getClass().getName()
                    )
            );
        }
        final Supplier<? extends Connector<?,?>> supplier = (Supplier<? extends Connector<?,?>>) attribute;
        final Connector<Object, ConnectorConfiguration> instance = (Connector<Object, ConnectorConfiguration>) supplier.get();
        LOGGER.debug(
                "Retrieved anonymous supplier {} with class {} for connection type {}",
                supplier,
                instance.name(),
                ctxt.getAttribute(ConnectorTypeResolver.CURRENT_SUPPLIER_TYPE_ID_KEY)
        );
        ctxt.setAttribute(
                ConnectorTypeResolver.CURRENT_SUPPLIER_KEY,
                null
        ).setAttribute(
                ConnectorTypeResolver.CURRENT_SUPPLIER_TYPE_ID_KEY,
                null
        );
        return instance;
    }
}
