package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;

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

    @Override
    public void configure(final HttpClient.Builder clientBuilder) {
        clientBuilder.authenticator(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        HTTPBasicAuthConfig.this.username,
                        HTTPBasicAuthConfig.this.password.toCharArray()
                );
            }
        });
    }
}
