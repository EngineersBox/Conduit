package com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http;


public class HTTPBasicAuthConfig extends HTTPAuthConfig {

    private static final String TYPE = "BASIC";

    private final String username;
    private final String password;

    public HTTPBasicAuthConfig(final String username,
                               final String password) {
        super(HTTPBasicAuthConfig.TYPE);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}
