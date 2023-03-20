package com.engineersbox.conduit.source.http;

import com.engineersbox.conduit.source.Source;
import com.engineersbox.conduit.source.SourceType;

import javax.net.ssl.SSLContext;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;

public class HTTPSource extends Source {

    private final HttpClient client;
    private final HTTPSourceAuthConfig authConfig;
    private final URI uri;

    public HTTPSource(final SourceType type,
                      final HTTPSourceAuthConfig authConfig,
                      final URI uri) {
        super(type);
        this.authConfig = authConfig;
        this.uri = uri;
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
                final SSLContext sslContext = SSLContext.getDefault();
                sslContext.
                builder.sslContext()
            }
        }
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                this.authConfig
                        );
                    }
                }).build()
    }

    @Override
    public String invoke() {

    }

}
