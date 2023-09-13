package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.AuthenticatorProvider;
import com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.SSLContextProvider;
import com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build.SSLParametersProvider;
import com.engineersbox.conduit.util.Functional;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    private static <T> void checkedApply(final Consumer<T> method,
                                         final Functional.ThrowsSupplier<T> supplier) throws Exception {
        if (supplier == null) {
            return;
        }
        checkedApply(method, supplier.get());
    }

    private static <T> void checkedApply(final Consumer<T> method,
                                         final T value) {
        if (value == null) {
            return;
        }
        method.accept(value);
    }

    @Override
    public void configure() throws Exception {
        final HttpClient.Builder builder = HttpClient.newBuilder();
        checkedApply(builder::authenticator, this.config.getAuthentication());
        checkedApply(builder::connectTimeout, this.config.getTimeout());
        checkedApply(builder::followRedirects, this.config.getRedirect());
        checkedApply(builder::localAddress, this.config.getLocalAddress());
        checkedApply(builder::priority, this.config.getPriority());
        checkedApply(builder::proxy, this.config.getProxy());
        checkedApply(builder::sslContext, this.config.getSSLContext());
        checkedApply(builder::sslParameters, this.config.getSSLParameters());
        checkedApply(builder::version, this.config.getVersion());
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
