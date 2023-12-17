package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.jmx;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.ssl.SSLContextProvider;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.ssl.SSLParametersProvider;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.ssl.factory.SSLParameterisedSocketFactory;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.security.auth.Subject;
import java.io.IOException;
import java.rmi.server.RMIClientSocketFactory;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Map;

/**
 * TODO: DOCSTRING
 *
 * In order to use SSL connections with a JMX connector, the environment
 * that is provided via the "environment" field, queried to JMXEnvironmentProvider
 * should set the following properties:
 * - {@link RMIConnectorServer#RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE} = An instance of {@link SslRMIClientSocketFactory}
 * - {@code "com.sun.jndi.rmi.factory.socket"} = An instance of {@link RMIClientSocketFactory} like {@link SslRMIClientSocketFactory}
 */
public class JMXConnector extends Connector<MBeanServerConnection, JMXConnectorConfiguration> {

    public static final String JSON_KEY = "JMX";
    private static final Logger LOGGER = LoggerFactory.getLogger(JMXConnector.class);
    private static final String JMX_REMOTE_PROFILES = "jmx.remote.profiles";
    private static final String JMX_TLS_SOCKET_FACTORY = "jmx.remote.tls.socket.factory";
    private static final String JMX_TLS_PROTOCOLS = "jmx.remote.tls.enabled.protocols";
    private static final String JMX_TLS_CIPHER_SUITES = "jmx.remote.tls.enabled.cipher.suites";

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

    @SuppressWarnings({"unchecked","BanJNDI"})
    @Override
    public void configure() throws Exception {
        final Map<String, Object> env = (Map<String,Object>) this.config.getEnvironment();
        configureSslEnv(env);
        try {
            this.connector = JMXConnectorFactory.connect(
                    this.config.getServiceUrl(),
                    env
            );
        } catch (final IOException e) {
            LOGGER.error("Unable to connect to remote JMX server", e);
            throw e;
        }
        final Subject delegationSubject = this.config.getDelegationSubject();
        if (delegationSubject != null) {
            this.serverConnection = this.connector.getMBeanServerConnection(delegationSubject);
        } else {
            this.serverConnection = this.connector.getMBeanServerConnection();
        }
    }

    private void configureSslEnv(final Map<String, Object> env) throws UnrecoverableKeyException,
                                                                       CertificateException,
                                                                       NoSuchAlgorithmException,
                                                                       KeyStoreException,
                                                                       IOException,
                                                                       NoSuchProviderException,
                                                                       KeyManagementException {
        // See: https://docs.oracle.com/cd/E19698-01/816-7609/security-83/index.html
        final SSLContextProvider sslContextProvider = this.config.getSSLContext();
        final SSLParametersProvider sslParametersProvider = this.config.sslParametersProvider();
        if (sslContextProvider != null && sslParametersProvider == null) {
            LOGGER.warn("JMXConnector: SSLContext provided without SSLParameters");
        }
        if (sslContextProvider == null) {
            return;
        }
        env.put(JMX_REMOTE_PROFILES, "TLS");
        if (sslParametersProvider != null) {
            final SSLParameters sslParameters = sslParametersProvider.get();
            // NOTE: Do we need to set protocols and suites since the factory sets
            //       the sslParameters on every socket created?
            env.put(JMX_TLS_PROTOCOLS, sslParameters.getProtocols());
            env.put(JMX_TLS_CIPHER_SUITES, sslParameters.getCipherSuites());
            final SSLSocketFactory sslSocketFactory = new SSLParameterisedSocketFactory(
                    sslContextProvider.get(),
                    sslParameters
            );
            env.put(JMX_TLS_SOCKET_FACTORY, sslSocketFactory);
        } else {
            LOGGER.warn("JMXConnector: SSL parameters not provided, assuming context only");
            env.put(JMX_TLS_SOCKET_FACTORY, sslContextProvider.get().getSocketFactory());
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
