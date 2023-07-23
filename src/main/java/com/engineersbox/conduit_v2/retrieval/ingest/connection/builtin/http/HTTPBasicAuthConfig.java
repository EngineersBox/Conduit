package com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HTTPBasicAuthConfig extends HTTPAuthConfig {

    private static final String TYPE = "BASIC";

    private final String username;
    private final String password;

    @JsonCreator
    public HTTPBasicAuthConfig(@JsonProperty("username") final String username,
                               @JsonProperty("password") final String password) {
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
