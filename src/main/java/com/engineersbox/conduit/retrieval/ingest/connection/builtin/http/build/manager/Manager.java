package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.manager;

public class Manager {

    protected final String keyStorePath;
    protected final String keyStorePassword;
    protected final String algorithm;
    protected final String provider;

    public Manager(final String keyStorePath,
                   final String keyStorePassword,
                   final String algorithm,
                   final String provider) {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.algorithm = algorithm;
        this.provider = provider;
    }

}
