package com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class HTTPConnector<T> implements Connector<T, HTTPConnectorConfiguration> {

    private HttpClient client;
    private HTTPConnectorConfiguration config;

    @Override
    public void configure(final HTTPConnectorConfiguration config) throws Exception {
        this.config = config;
        final HttpClient.Builder builder = HttpClient.newBuilder();
        switch (config.getAuthConfig().getAuthType()) {
            case BASIC -> {
                final HTTPBasicAuthConfig basicAuth = (HTTPBasicAuthConfig) config.getAuthConfig();
                builder.authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                basicAuth.getUsername(),
                                basicAuth.getPassword().toCharArray()
                        );
                    }
                });
            }
            case CERTIFICATE -> {
                final HTTPCertificateAuthConfig certAuth = (HTTPCertificateAuthConfig) config.getAuthConfig();
                try {
                    final SSLContext sslContext = SSLContext.getInstance("TLS");
                    final TrustManagerFactory trustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    final KeyStore cert = KeyStore.getInstance(certAuth.getCertType().name());
                    final InputStream certStream = new FileInputStream(certAuth.getCertificatePath().toAbsolutePath().toString());
                    cert.load(
                            certStream,
                            certAuth.getCertPassword()
                    );
                    certStream.close();
                    trustManager.init(cert);
                    sslContext.init(
                            null,
                            trustManager.getTrustManagers(),
                            null
                    );
                    builder.sslContext(sslContext);
                } catch (final NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException |
                               KeyManagementException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        this.client = builder.build();
    }

    @Override
    public T retrieve() throws Exception {
        if (this.client == null || this.config == null) {
            throw new IOException("Connector has not been configured, HTTP client has not be created");
        }
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(this.config.getUri())
                .build();
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
