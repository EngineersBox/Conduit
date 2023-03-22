package com.engineersbox.conduit.source.http;

import com.engineersbox.conduit.pipeline.ingestion.IngestionContext;
import com.engineersbox.conduit.source.Source;
import com.engineersbox.conduit.source.SourceType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class HTTPSource extends Source {

    private final HttpClient client;
    private final HttpRequest request;
    private final HTTPSourceAuthConfig authConfig;
    private final URI uri;

    public HTTPSource(final SourceType type,
                      final HTTPSourceAuthConfig authConfig,
                      final URI uri) {
        super(type);
        this.authConfig = authConfig;
        this.uri = uri;
        this.client = constructClient();
        this.request = buildRequest();
    }

    public HTTPSourceAuthConfig getAuthConfig() {
        return this.authConfig;
    }

    public URI getUri() {
        return this.uri;
    }

    private HttpClient constructClient() {
        final HttpClient.Builder builder = HttpClient.newBuilder();
        switch (this.authConfig.getAuthType()) {
            case BASIC -> {
                final HTTPSourceBasicAuthConfig basicAuth = (HTTPSourceBasicAuthConfig) this.authConfig;
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
                final HTTPSourceCertificateAuthConfig certAuth = (HTTPSourceCertificateAuthConfig) this.authConfig;
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
        return builder.build();
    }

    private HttpRequest buildRequest() {
        return HttpRequest.newBuilder()
                .GET()
                .uri(this.uri)
                .build();
    }

    @Override
    public String invoke(final IngestionContext ctx) {
        final HttpResponse<String> response;
        try {
            response = this.client.send(
                    this.request,
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (final IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        final String body = response.body();
        if (body == null) {
            throw new IllegalStateException(String.format(
                    "Unable to retrieve data from endpoint \"%s\"",
                    this.uri.getPath()
            ));
        }
        return body;
    }

}
