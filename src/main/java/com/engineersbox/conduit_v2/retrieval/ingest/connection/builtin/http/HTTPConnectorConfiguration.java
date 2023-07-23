package com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;

import java.net.URI;

public class HTTPConnectorConfiguration implements ConnectorConfiguration {

    private final URI uri;
    private final HTTPAuthConfig auth;

    public HTTPConnectorConfiguration(final URI uri,
                                      final HTTPAuthConfig auth) {
        this.uri = uri;
        this.auth = auth;
    }

    public URI getUri() {
        return uri;
    }

    public HTTPAuthConfig getAuth() {
        return auth;
    }
}
