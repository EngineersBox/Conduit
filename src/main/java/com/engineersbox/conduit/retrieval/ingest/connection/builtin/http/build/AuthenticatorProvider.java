package com.engineersbox.conduit.retrieval.ingest.connection.builtin.http.build;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.function.Supplier;

public class AuthenticatorProvider implements Supplier<Authenticator> {

    private final String username;
    private final String password;

    @JsonCreator
    public AuthenticatorProvider(@JsonProperty("username") final String username,
                                 @JsonProperty("password") final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Authenticator get() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        AuthenticatorProvider.this.username,
                        AuthenticatorProvider.this.password.toCharArray()
                );
            }
        };
    }
}
