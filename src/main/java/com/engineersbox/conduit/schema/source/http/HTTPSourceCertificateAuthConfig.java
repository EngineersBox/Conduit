package com.engineersbox.conduit.schema.source.http;

import java.nio.file.Path;

public class HTTPSourceCertificateAuthConfig extends HTTPSourceAuthConfig {

    private final HTTPCertType certType;
    private final char[] certPassword;
    private final Path certificatePath;

    public HTTPSourceCertificateAuthConfig(final HTTPCertType certType,
                                           final String certPassword,
                                           final String certificatePath) {
        super(HTTPAuthType.CERTIFICATE);
        this.certificatePath = Path.of(certificatePath);
        this.certPassword = certPassword == null ? null : certPassword.toCharArray();
        this.certType = certType;
    }

    public HTTPSourceCertificateAuthConfig(final HTTPCertType certType,
                                           final String certPassword,
                                           final Path certificatePath) {
        super(HTTPAuthType.CERTIFICATE);
        this.certificatePath = certificatePath;
        this.certPassword = certPassword == null ? null : certPassword.toCharArray();
        this.certType = certType;
    }

    public Path getCertificatePath() {
        return this.certificatePath;
    }

    public HTTPCertType getCertType() {
        return this.certType;
    }

    public char[] getCertPassword() {
        return this.certPassword;
    }

}
