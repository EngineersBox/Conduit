package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http;

import com.engineersbox.conduit.retrieval.ingest.connection.ConnectorConfiguration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.net.URI;

public class HTTPConnectorConfiguration implements ConnectorConfiguration {

    private final URI uri;
    private final HTTPAuthConfig auth;

    @JsonCreator
    public HTTPConnectorConfiguration(@JsonProperty("uri") @JsonDeserialize(using = UriDeserialiser.class)final URI uri,
                                      @JsonProperty("auth") final HTTPAuthConfig auth) {
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
