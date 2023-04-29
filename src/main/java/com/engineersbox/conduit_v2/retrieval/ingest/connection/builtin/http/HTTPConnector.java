package com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;

import java.net.http.HttpClient;

public class HTTPConnector<T> implements Connector<T, HTTPConnectorConfiguration> {

    private HttpClient client;


    @Override
    public void configure(final HTTPConnectorConfiguration config) throws Exception {

    }

    @Override
    public T retrieve() throws Exception {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
