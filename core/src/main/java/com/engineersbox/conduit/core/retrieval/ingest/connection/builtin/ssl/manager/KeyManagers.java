package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.ssl.manager;

import com.engineersbox.conduit.core.util.Functional;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class KeyManagers extends Manager implements Functional.ThrowsSupplier<KeyManager[]> {

    @JsonCreator
    public KeyManagers(@JsonProperty(value ="storePath", required = true) @JsonAlias("store_path") final String storePath,
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
    public KeyManager[] get() throws NoSuchAlgorithmException, NoSuchProviderException, CertificateException, KeyStoreException, IOException, UnrecoverableKeyException {
        final KeyManagerFactory factory = KeyManagerFactory.getInstance(
                algorithm,
                provider
        );
        factory.init(
                KeyStore.getInstance(
                        new File(this.storePath),
                        this.storePassword.toCharArray()
                ),
                this.storePassword.toCharArray()
        );
        return factory.getKeyManagers();
    }
}
