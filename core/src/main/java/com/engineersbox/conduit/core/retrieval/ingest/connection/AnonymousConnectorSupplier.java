package com.engineersbox.conduit.core.retrieval.ingest.connection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = AnonymousConnectorDeserializer.class)
public class AnonymousConnectorSupplier implements Connector<Connector<Object, ConnectorConfiguration>, ConnectorConfiguration> {

    @Override
    public void saturate(final ConnectorConfiguration config) {
        throw new UnsupportedOperationException("Config saturation is not supported on anonymous supplier");
    }

    @Override
    public void configure() throws Exception {
        throw new UnsupportedOperationException("Config application is not supported on anonymous supplier");
    }

    @Override
    public Connector<Object, ConnectorConfiguration> retrieve() throws Exception {
        throw new UnsupportedOperationException("Connector retrieval is not supported on anonymous supplier");
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException("Closing is not supported on anonymous supplier");
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }
}
