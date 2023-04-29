package com.engineersbox.conduit.schema.source.http;

public class HTTPSourceBasicAuthConfig extends HTTPSourceAuthConfig {

    private final String username;
    private final String password;

    public HTTPSourceBasicAuthConfig(final String username,
                                     final String password) {
        super(HTTPAuthType.BASIC);
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
