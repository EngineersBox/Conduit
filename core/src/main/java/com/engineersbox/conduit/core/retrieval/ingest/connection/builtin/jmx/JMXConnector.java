package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JMXConnector implements Connector<String, JMXConnectorConfiguration> {

    public static final String JSON_KEY = "JMX";

    private JMXConnectorConfiguration config;

    public JMXConnector(@JsonProperty("config") final JMXConnectorConfiguration config) {
        this.config = config;
    }

    @Override
    public void saturate(final JMXConnectorConfiguration config) {
        this.config = config;
    }

    @Override
    public void configure() throws Exception {

    }

    @Override
    public String retrieve() throws Exception {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
