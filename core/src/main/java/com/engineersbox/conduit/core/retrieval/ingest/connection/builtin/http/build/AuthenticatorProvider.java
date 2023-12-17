package com.engineersbox.conduit.core.retrieval.ingest.connection.builtin.http.build;

import com.engineersbox.conduit.core.util.Functional;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class AuthenticatorProvider implements Functional.ThrowsSupplier<Authenticator> {

    private final String username;
    private final char[] password;

    @JsonCreator
    public AuthenticatorProvider(@JsonProperty(value = "username", required = true) final String username,
                                 @JsonProperty("password") final String password) {
        this.username = username;
        this.password = password == null ? new char[0] : password.toCharArray() ;
    }

    @Override
    public Authenticator get() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        AuthenticatorProvider.this.username,
                        AuthenticatorProvider.this.password
                );
            }
        };
    }
}
