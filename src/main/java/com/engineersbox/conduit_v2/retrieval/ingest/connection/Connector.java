package com.engineersbox.conduit_v2.retrieval.ingest.connection;

public interface Connector<T, C extends ConnectorConfiguration> extends AutoCloseable {

    void saturate(final C config);

    void configure() throws Exception;

    T retrieve() throws Exception;

}
