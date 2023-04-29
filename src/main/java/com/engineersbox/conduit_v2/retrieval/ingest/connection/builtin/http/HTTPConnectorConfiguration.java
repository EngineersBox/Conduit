package com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;

import java.net.URI;

public class HTTPConnectorConfiguration implements ConnectorConfiguration {

    private final URI uri;
    private final HTTPAuthConfig authConfig;

    public HTTPConnectorConfiguration(final URI uri,
                                      final HTTPAuthConfig authConfig) {
        this.uri = uri;
        this.authConfig = authConfig;
    }

    public URI getUri() {
        return uri;
    }

    public HTTPAuthConfig getAuthConfig() {
        return authConfig;
    }
}
