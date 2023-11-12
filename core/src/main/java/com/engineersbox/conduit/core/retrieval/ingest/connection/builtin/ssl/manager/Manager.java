package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.ssl.manager;

public class Manager {

    protected final String storePath;
    protected final String storePassword;
    protected final String algorithm;
    protected final String provider;

    public Manager(final String storePath,
                   final String storePassword,
                   final String algorithm,
                   final String provider) {
        this.storePath = storePath;
        this.storePassword = storePassword;
        this.algorithm = algorithm;
        this.provider = provider;
    }

}
