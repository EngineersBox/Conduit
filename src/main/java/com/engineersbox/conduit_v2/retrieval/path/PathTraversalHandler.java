package com.engineersbox.conduit_v2.retrieval.path;

import com.jayway.jsonpath.*;

import java.io.InputStream;

public class PathTraversalHandler<R> {

    private final ParseContext parseContext;
    private ReadContext context;

    public PathTraversalHandler(final Configuration configuration) {
        this.parseContext = JsonPath.using(configuration);
    }

    public void saturate(final R raw) {
        if (raw instanceof String rawString) {
            this.parseContext.parse(rawString);
        } else if (raw instanceof InputStream rawInputStream) {
            this.parseContext.parse(rawInputStream);
        } else if (raw instanceof byte[] rawBytes) {
            this.parseContext.parse(rawBytes);
        } else {
            this.parseContext.parse(raw);
        }
    }

    public <T> T read(final String path,
                      final TypeRef<T> type) {
        return this.context.read(path, type);
    }

}
