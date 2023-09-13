package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class HTTPCertificateAuthConfig extends HTTPAuthConfig {

    private static final String TYPE = "CERTIFICATE";

    private final String certificateType;
    private final char[] certificatePassword;
    private final Path certificatePath;

    @JsonCreator
    public HTTPCertificateAuthConfig(@JsonProperty("certificateType") @JsonAlias("certificate_type")final String certificateType,
                                     @JsonProperty("certificatePassword") @JsonAlias("certificate_password")final String certificatePassword,
                                     @JsonProperty("certificatePath") @JsonAlias("certificate_path")final String certificatePath) {
        super(HTTPCertificateAuthConfig.TYPE);
        this.certificatePath = Path.of(certificatePath);
        this.certificatePassword = certificatePassword == null ? null : certificatePassword.toCharArray();
        this.certificateType = certificateType;
    }

    public HTTPCertificateAuthConfig(final String certificateType,
                                     final String certificatePassword,
                                     final Path certificatePath) {
        super(HTTPCertificateAuthConfig.TYPE);
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword == null ? null : certificatePassword.toCharArray();
        this.certificateType = certificateType;
    }

    public Path getCertificatePath() {
        return this.certificatePath;
    }

    public String getCertificateType() {
        return this.certificateType;
    }

    public char[] getCertificatePassword() {
        return this.certificatePassword;
    }

    @Override
    public void configure(final HttpClient.Builder clientBuilder) {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            final TrustManagerFactory trustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            final KeyStore cert = KeyStore.getInstance(this.certificateType);
            final InputStream certStream = new FileInputStream(this.certificatePath.toAbsolutePath().toString());
            cert.load(
                    certStream,
                    this.certificatePassword
            );
            certStream.close();
            trustManager.init(cert);
            sslContext.init(
                    null,
                    trustManager.getTrustManagers(),
                    null
            );
            clientBuilder.sslContext(sslContext);
        } catch (final NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException |
                       KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
