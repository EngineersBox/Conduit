package com.engineersbox.conduit.retrieval.path;

import com.jayway.jsonpath.*;

import java.io.InputStream;

public class PathTraversalHandler<R> {

    /* TODO: Allow for configurations to be retrieved based on affinity ID
     *       which requires using the JsonPath.parse(raw, config) methods
     *       instead of the JsonPath.using(config).parse(raw) handlers.
     *       Each time the saturate method is called, the config should
     *       be retrieved based on the affinity id and then the raw data
     *       parsed with it.
     */

    private final ParseContext parseContext;
    private ReadContext context;

    public PathTraversalHandler(final Configuration configuration) {
        this.parseContext = JsonPath.using(configuration);
    }

    public void saturate(final R raw) {
        if (raw instanceof String rawString) {
            this.context = this.parseContext.parse(rawString);
        } else if (raw instanceof InputStream rawInputStream) {
            this.context = this.parseContext.parse(rawInputStream);
        } else if (raw instanceof byte[] rawBytes) {
            this.context = this.parseContext.parseUtf8(rawBytes);
        } else {
            this.context = this.parseContext.parse(raw);
        }
    }

    public <T> T read(final String path,
                      final TypeRef<T> type) {
        return this.context.read(path, type);
    }

}
