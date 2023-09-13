package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build;

import com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.manager.KeyManagers;
import com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.manager.TrustManagers;
import com.engineersbox.conduit.util.Functional;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLContextProvider implements Functional.ThrowsSupplier<SSLContext> {

    private final String protocol;
    private final String provider;
    private final KeyManagers keyManagers;
    private final TrustManagers trustManagers;
    private final String secureRandomAlgorithm;
    private final String secureRandomProvider;

    @JsonCreator
    public SSLContextProvider(@JsonProperty("protocol") final String protocol,
                              @JsonProperty("provider") final String provider,
                              @JsonProperty("keyManager") @JsonAlias("key_manager") final KeyManagers keyManagers,
                              @JsonProperty("trustManager") @JsonAlias("trust_manager") final TrustManagers trustManagers,
                              @JsonProperty("secureRandomAlgorithm") @JsonAlias("secure_random_algorithm") final String secureRandomAlgorithm,
                              @JsonProperty("secureRandomProvider") @JsonAlias("secure_random_provider") final String secureRandomProvider) {
        this.protocol = protocol;
        this.provider = provider;
        this.keyManagers = keyManagers;
        this.trustManagers = trustManagers;
        this.secureRandomAlgorithm = secureRandomAlgorithm;
        this.secureRandomProvider = secureRandomProvider;
    }

    @Override
    public SSLContext get() throws NoSuchAlgorithmException, NoSuchProviderException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, KeyManagementException {
        final SSLContext context = SSLContext.getInstance(
                this.protocol,
                this.provider
        );
        SecureRandom random = null;
        if (this.secureRandomAlgorithm != null) {
            random = SecureRandom.getInstance(
                    this.secureRandomAlgorithm,
                    this.secureRandomProvider
            );
        }
        context.init(
                this.keyManagers.get(),
                this.trustManagers.get(),
                random
        );
        return context;
    }

}
