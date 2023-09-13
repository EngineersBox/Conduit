package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit.retrieval.ingest.connection.Connector;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HTTPConnector implements Connector<String, HTTPConnectorConfiguration> {

    @JsonIgnore
    private HttpClient client;
    private HTTPConnectorConfiguration config;

    @JsonCreator
    public HTTPConnector(@JsonProperty("config") final HTTPConnectorConfiguration config) {
        saturate(config);
    }

    public void saturate(final HTTPConnectorConfiguration config) {
        this.config = config;
    }

    @Override
    public void configure() throws Exception {
        final HttpClient.Builder builder = HttpClient.newBuilder();
        this.config.getAuth().configure(builder);
        this.client = builder.build();
    }

    @Override
    public String retrieve() throws Exception {
        if (this.client == null || this.config == null) {
            throw new IOException("Connector has not been configured, HTTP client has not be created");
        }
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(this.config.getUri())
                .build();
        return this.client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        ).body();
    }

    @Override
    public void close() throws Exception {
    }
}
