package com.engineersbox.conduit_v2.retrieval.ingest;

public interface Connector<T> extends AutoCloseable {

    void configure() throws Exception;

    T retrieve() throws Exception;

}
