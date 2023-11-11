package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.security.auth.Subject;
import java.rmi.server.RMIClientSocketFactory;

/**
 * TODO: DOCSTRING
 *
 * In order to use SSL connections with a JMX connector, the environment
 * that is provided via the "environment" field, queried to JMXEnvironmentProvider
 * should set the following properties:
 * - {@link RMIConnectorServer#RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE} = An instance of {@link SslRMIClientSocketFactory}
 * - {@code "com.sun.jndi.rmi.factory.socket"} = An instance of {@link RMIClientSocketFactory}
 */
public class JMXConnector extends Connector<MBeanServerConnection, JMXConnectorConfiguration> {

    public static final String JSON_KEY = "JMX";

    @JsonIgnore
    private javax.management.remote.JMXConnector connector;
    @JsonIgnore
    private MBeanServerConnection serverConnection;
    private JMXConnectorConfiguration config;

    @JsonCreator
    public JMXConnector(@JsonProperty("config") final JMXConnectorConfiguration config) {
        this.config = config;
    }

    @Override
    public void saturate(final JMXConnectorConfiguration config) {
        this.config = config;
    }

    @Override
    public void configure() throws Exception {
        this.connector = JMXConnectorFactory.connect(
                this.config.getServiceUrl(),
                this.config.getEnvironment()
        );
        final Subject delegationSubject = this.config.getDelegationSubject();
        if (delegationSubject != null) {
            this.serverConnection = this.connector.getMBeanServerConnection(delegationSubject);
        } else {
            this.serverConnection = this.connector.getMBeanServerConnection();
        }
    }

    @Override
    public MBeanServerConnection retrieve() {
        return this.serverConnection;
    }

    @Override
    public void close() throws Exception {
        this.connector.close();
    }
}
