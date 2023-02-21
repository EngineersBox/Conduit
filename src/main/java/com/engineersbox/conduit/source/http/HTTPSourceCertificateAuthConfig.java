package com.engineersbox.conduit.source.http;

import java.nio.file.Path;

public class HTTPSourceCertificateAuthConfig extends HTTPSourceAuthConfig {

    private final Path certificatePath;

    public HTTPSourceCertificateAuthConfig(final String certificatePath) {
        super(HTTPAuthType.CERTIFICATE);
        this.certificatePath = Path.of(certificatePath);
    }

    public HTTPSourceCertificateAuthConfig(final Path certificatePath) {
        super(HTTPAuthType.CERTIFICATE);
        this.certificatePath = certificatePath;
    }

    public Path getCertificatePath() {
        return this.certificatePath;
    }
}
