package com.engineersbox.conduit.core.retrieval.path;

import com.engineersbox.conduit.core.processing.task.worker.ClientBoundForkJoinWorkerThead;
import com.engineersbox.conduit.core.retrieval.configuration.AffinityBoundConfigProvider;
import com.jayway.jsonpath.*;

import java.io.InputStream;

public class PathTraversalHandler<R> {

    private ReadContext context;
    private ParseContext parseContext;
    private Configuration config;
    private final boolean cachedConfig;

    private final boolean affinityBoundConfig;

    public PathTraversalHandler(final Configuration config) {
        this.config = config;
        this.cachedConfig = false;
        this.affinityBoundConfig = false;
        this.parseContext = JsonPath.using(config);
    }

    public PathTraversalHandler(final boolean cachedConfig) {
        this.cachedConfig = cachedConfig;
        this.affinityBoundConfig = true;
    }

    public void saturate(final R raw) {
        if (this.affinityBoundConfig && (!cachedConfig || this.config == null)) {
            this.config = AffinityBoundConfigProvider.getConfiguration(
                    ClientBoundForkJoinWorkerThead.getThreadAffinityId()
            );
        }
        final ParseContext ctx;
        if (this.affinityBoundConfig) {
            ctx = JsonPath.using(this.config);
        } else {
            ctx = this.parseContext;
        }
        if (raw instanceof String rawString) {
            this.context = ctx.parse(rawString);
        } else if (raw instanceof InputStream rawInputStream) {
            this.context = ctx.parse(rawInputStream);
        } else if (raw instanceof byte[] rawBytes) {
            this.context = ctx.parseUtf8(rawBytes);
        } else {
            this.context = ctx.parse(raw);
        }
    }

    public <T> T read(final String path,
                      final TypeRef<T> type) {
        return this.context.read(path, type);
    }

}
