package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http.build.manager;

import com.engineersbox.conduit.core.util.Functional;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

public class TrustManagers extends Manager implements Functional.ThrowsSupplier<TrustManager[]> {

    @JsonCreator
    public TrustManagers(@JsonProperty("keyStorePath") @JsonAlias("key_store_path") final String keyStorePath,
                         @JsonProperty("keyStorePassword") @JsonAlias("key_store_password") final String keyStorePassword,
                         @JsonProperty("algorithm") final String algorithm,
                         @JsonProperty("provider") final String provider) {
        super(
                keyStorePath,
                keyStorePassword,
                algorithm,
                provider
        );
    }

    @Override
    public TrustManager[] get() throws NoSuchAlgorithmException, NoSuchProviderException, CertificateException, KeyStoreException, IOException {
        final TrustManagerFactory factory = TrustManagerFactory.getInstance(
                this.algorithm,
                this.provider
        );
        final KeyStore keyStore = KeyStore.getInstance(
                new File(this.keyStorePath),
                this.keyStorePassword.toCharArray()
        );
        factory.init(keyStore);
        return factory.getTrustManagers();
    }
}
