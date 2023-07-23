package com.engineersbox.conduit_v2.retrieval.ingest.connection.builtin.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

public class HTTPCertificateAuthConfig extends HTTPAuthConfig {

    private static final String TYPE = "CERTIFICATE";

    private final String certificateType;
    private final char[] certificatePassword;
    private final Path certificatePath;

    @JsonCreator
    public HTTPCertificateAuthConfig(@JsonProperty("certificateType") @JsonAlias("certificate_type")final String certificateType,
                                     @JsonProperty("certificatePassword") @JsonAlias("certificate_password")final String certificatePassword,
                                     @JsonProperty("certificatePath") @JsonAlias("certificate_path")final String certificatePath) {
        super(HTTPCertificateAuthConfig.TYPE);
        this.certificatePath = Path.of(certificatePath);
        this.certificatePassword = certificatePassword == null ? null : certificatePassword.toCharArray();
        this.certificateType = certificateType;
    }

    public HTTPCertificateAuthConfig(final String certificateType,
                                     final String certificatePassword,
                                     final Path certificatePath) {
        super(HTTPCertificateAuthConfig.TYPE);
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword == null ? null : certificatePassword.toCharArray();
        this.certificateType = certificateType;
    }

    public Path getCertificatePath() {
        return this.certificatePath;
    }

    public String getCertificateType() {
        return this.certificateType;
    }

    public char[] getCertificatePassword() {
        return this.certificatePassword;
    }

}
