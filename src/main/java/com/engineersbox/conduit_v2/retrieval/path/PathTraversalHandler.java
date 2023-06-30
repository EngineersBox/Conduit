package com.engineersbox.conduit_v2.retrieval.path;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JsonProvider;

public class PathTraversalHandler<R> {

    private final ParseContext parseContext;
    private final JsonProvider provider;
    private ReadContext context;

    public PathTraversalHandler(final Configuration configuration) {
        this.parseContext = JsonPath.using(configuration);
        this.provider = configuration.jsonProvider();
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
