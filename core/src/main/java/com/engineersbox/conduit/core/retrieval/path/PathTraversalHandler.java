package com.engineersbox.conduit.core.retrieval.path;

import com.engineersbox.conduit.core.processing.task.worker.ClientBoundForkJoinWorkerThead;
import com.engineersbox.conduit.core.retrieval.configuration.AffinityBoundConfigProvider;
import com.jayway.jsonpath.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class PathTraversalHandler<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathTraversalHandler.class);

    private ThreadLocal<ReadContext> context;
    private ParseContext parseContext;
    private Configuration config;
    private final boolean cachedConfig;
    private final boolean affinityBoundConfig;

    public PathTraversalHandler(final Configuration config) {
        this.config = config;
        this.cachedConfig = false;
        this.affinityBoundConfig = false;
        this.parseContext = JsonPath.using(config);
        logConfiguration();
    }

    public PathTraversalHandler(final boolean cachedConfig) {
        this.cachedConfig = cachedConfig;
        this.affinityBoundConfig = true;
        logConfiguration();
    }

    private void logConfiguration() {
        LOGGER.trace(
                "PathTraversalHandler configuration [Cached Config: {}] [Affinity Bound Config: {}]",
                this.cachedConfig,
                this.affinityBoundConfig
        );
    }

    public void saturate(final R raw) {
        if (this.affinityBoundConfig && (!cachedConfig || this.config == null)) {
            final long affinityId = ClientBoundForkJoinWorkerThead.getThreadAffinityId();
            this.config = AffinityBoundConfigProvider.getConfiguration(affinityId);
            LOGGER.trace(
                    "Retrieved affinity bound config {} with origin id {}",
                    this.config,
                    affinityId
            );
        }
        final ParseContext ctx;
        if (this.affinityBoundConfig) {
            ctx = JsonPath.using(this.config);
        } else {
            ctx = this.parseContext;
        }
        if (raw instanceof String rawString) {
            this.context.set(ctx.parse(rawString));
        } else if (raw instanceof InputStream rawInputStream) {
            this.context.set(ctx.parse(rawInputStream));
        } else if (raw instanceof byte[] rawBytes) {
            this.context.set(ctx.parseUtf8(rawBytes));
        } else {
            this.context.set(ctx.parse(raw));
        }
    }

    public <T> T read(final String path,
                      final TypeRef<T> type) {
        return this.context.get().read(path, type);
    }

}
