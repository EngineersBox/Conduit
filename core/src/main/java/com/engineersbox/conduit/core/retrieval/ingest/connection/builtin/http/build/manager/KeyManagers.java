package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http.build.manager;

import com.engineersbox.conduit.core.util.Functional;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class KeyManagers extends Manager implements Functional.ThrowsSupplier<KeyManager[]> {

    @JsonCreator
    public KeyManagers(@JsonProperty("keyStorePath") @JsonAlias("key_store_path") final String keyStorePath,
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
    public KeyManager[] get() throws NoSuchAlgorithmException, NoSuchProviderException, CertificateException, KeyStoreException, IOException, UnrecoverableKeyException {
        final KeyManagerFactory factory = KeyManagerFactory.getInstance(
                algorithm,
                provider
        );
        factory.init(
                KeyStore.getInstance(
                        new File(this.keyStorePath),
                        this.keyStorePassword.toCharArray()
                ),
                this.keyStorePassword.toCharArray()
        );
        return factory.getKeyManagers();
    }
}
