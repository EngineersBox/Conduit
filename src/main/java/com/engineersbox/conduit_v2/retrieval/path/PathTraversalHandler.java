package com.engineersbox.conduit_v2.retrieval.path;

import com.jayway.jsonpath.*;

public class PathTraversalHandler<R> {

    private final ParseContext parseContext;
    private ReadContext context;

    public PathTraversalHandler(final Configuration configuration) {
        this.parseContext = JsonPath.using(configuration);
    }

    public void saturate(final R raw) {
        this.context = raw instanceof String rawString
                ? this.parseContext.parse(rawString)
                : this.parseContext.parse(raw);
    }

    public <T> T read(final String path,
                      final TypeRef<T> type) {
        return this.context.read(path, type);
    }

}
