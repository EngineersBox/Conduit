package com.engineersbox.conduit.source.http;

import com.engineersbox.conduit.source.Source;
import com.engineersbox.conduit.source.SourceType;

import java.net.URI;

public class HTTPSource extends Source {

    private final HTTPSourceAuthConfig authConfig;
    private final URI uri;

    public HTTPSource(final SourceType type,
                      final HTTPSourceAuthConfig authConfig,
                      final URI uri) {
        super(type);
        this.authConfig = authConfig;
        this.uri = uri;
    }

    public HTTPSourceAuthConfig getAuthConfig() {
        return this.authConfig;
    }

    public URI getUri() {
        return this.uri;
    }
}
