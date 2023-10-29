package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.util.Functional;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HTTPConnector extends Connector<String, HTTPConnectorConfiguration> {

    public static final String JSON_KEY = "HTTP";

    @JsonIgnore
    private HttpClient client;
    private HTTPConnectorConfiguration config;

    @JsonCreator
    public HTTPConnector(@JsonProperty("config") final HTTPConnectorConfiguration config) {
        saturate(config);
    }

    @Override
    public void saturate(final HTTPConnectorConfiguration config) {
        this.config = config;
    }

    @Override
    public void configure() throws Exception {
        final HttpClient.Builder builder = HttpClient.newBuilder();
        Functional.checkedApply(builder::authenticator, this.config.getAuthentication());
        Functional.checkedApply(builder::connectTimeout, this.config.getTimeout());
        Functional.checkedApply(builder::followRedirects, this.config.getRedirect());
        Functional.checkedApply(builder::localAddress, this.config.getLocalAddress());
        Functional.checkedApply(builder::priority, this.config.getPriority());
        Functional.checkedApply(builder::proxy, this.config.getProxy());
        Functional.checkedApply(builder::sslContext, this.config.getSSLContext());
        Functional.checkedApply(builder::sslParameters, this.config.getSSLParameters());
        Functional.checkedApply(builder::version, this.config.getVersion());
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
