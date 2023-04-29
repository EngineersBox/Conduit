package com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http;

public class HTTPAuthConfig {

    private final HTTPAuthType authType;

    public HTTPAuthConfig(final HTTPAuthType authType) {
        this.authType = authType;
    }

    public HTTPAuthType getAuthType() {
        return authType;
    }
}
