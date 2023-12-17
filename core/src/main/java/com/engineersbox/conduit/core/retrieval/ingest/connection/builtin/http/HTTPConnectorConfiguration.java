package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http.build.AuthenticatorProvider;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.proxy.ProxySelectorDeserialiser;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.ssl.SSLContextProvider;
import com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.ssl.SSLParametersProvider;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

public class HTTPConnectorConfiguration implements ConnectorConfiguration {

    private final URI uri;
    private final AuthenticatorProvider authentication;
    private final SSLContextProvider sslContext;
    private final SSLParametersProvider sslParameters;
    private final Duration timeout;
    private final HttpClient.Redirect redirect;
    private final Integer priority;
    private final HttpClient.Version version;
    private final ProxySelector proxy;
    private final InetAddress localAddress;

    @JsonCreator
    public HTTPConnectorConfiguration(@JsonProperty(value = "uri", required = true) @JsonDeserialize(using = UriDeserialiser.class) final URI uri,
                                      @JsonProperty("authentication") final AuthenticatorProvider authentication,
                                      @JsonProperty("sslContext") @JsonAlias("ssl_context") final SSLContextProvider sslContext,
                                      @JsonProperty("sslParameters") @JsonAlias("ssl_parameters") final SSLParametersProvider sslParameters,
                                      @JsonProperty("timeout") final Duration timeout,
                                      @JsonProperty("redirect") final HttpClient.Redirect redirect,
                                      @JsonProperty("priority") final Integer priority,
                                      @JsonProperty("version") final HttpClient.Version version,
                                      @JsonProperty("proxy") @JsonDeserialize(using = ProxySelectorDeserialiser.class) final ProxySelector proxy,
                                      @JsonProperty("localAddress") @JsonAlias("local_address") final InetAddress localAddress) {
        this.uri = uri;
        this.authentication = authentication;
        this.sslContext = sslContext;
        this.sslParameters = sslParameters;
        this.timeout = timeout;
        this.redirect = redirect;
        this.priority = priority;
        this.version = version;
        this.proxy = proxy;
        this.localAddress = localAddress;
    }

    public URI getUri() {
        return uri;
    }

    public AuthenticatorProvider getAuthentication() {
        return this.authentication;
    }

    public SSLContextProvider getSSLContext() {
        return this.sslContext;
    }

    public SSLParametersProvider getSSLParameters() {
        return this.sslParameters;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    public HttpClient.Redirect getRedirect() {
        return this.redirect;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public HttpClient.Version getVersion() {
        return this.version;
    }

    public ProxySelector getProxy() {
        return this.proxy;
    }

    public InetAddress getLocalAddress() {
        return this.localAddress;
    }
}
