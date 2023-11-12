package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http.build.manager;

import com.engineersbox.conduit.core.util.Functional;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
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
    public TrustManagers(@JsonProperty(value = "storePath", required = true) @JsonAlias("store_path") final String storePath,
                         @JsonProperty("storePassword") @JsonAlias("store_password") @Nullable final String storePassword,
                         @JsonProperty(value = "algorithm", required = true) final String algorithm,
                         @JsonProperty("provider") @Nullable final String provider) {
        super(
                storePath,
                storePassword,
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
                new File(this.storePath),
                this.storePassword.toCharArray()
        );
        factory.init(keyStore);
        return factory.getTrustManagers();
    }
}
