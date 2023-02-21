package com.engineersbox.conduit.source.http;

public class HTTPSourceAuthConfig {

    private final HTTPAuthType authType;

    public HTTPSourceAuthConfig(final HTTPAuthType authType) {
        this.authType = authType;
    }

    public HTTPAuthType getAuthType() {
        return authType;
    }
}
