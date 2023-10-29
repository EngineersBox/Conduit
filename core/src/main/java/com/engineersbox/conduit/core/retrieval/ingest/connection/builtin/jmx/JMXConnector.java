package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXConnector extends Connector<String, JMXConnectorConfiguration> {

    public static final String JSON_KEY = "JMX";

    @JsonIgnore
    private javax.management.remote.JMXConnector connector;
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
        final JMXServiceURL serviceURL = new JMXServiceURL("");
        this.connector = JMXConnectorFactory.connect(serviceURL, null);
    }

    @Override
    public String retrieve() throws Exception {
        return null;
    }

    @Override
    public void close() throws Exception {
        connector.close();
    }
}
