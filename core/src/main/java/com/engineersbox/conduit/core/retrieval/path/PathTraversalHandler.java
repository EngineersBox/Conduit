package com.engineersbox.conduit.core.retrieval.path;

import com.engineersbox.conduit.core.processing.task.worker.ClientBoundForkJoinWorkerThead;
import com.engineersbox.conduit.core.retrieval.configuration.AffinityBoundConfigProvider;
import com.jayway.jsonpath.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PathTraversalHandler<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathTraversalHandler.class);
    private ReadContext context;
    private Configuration config;
    private final boolean cachedConfig;

    public PathTraversalHandler(final boolean cachedConfig) {
        this.cachedConfig = cachedConfig;
    }

    public void saturate(final R raw) {
        if (!cachedConfig || this.config == null) {
            this.config = AffinityBoundConfigProvider.getConfiguration(
                    ClientBoundForkJoinWorkerThead.getThreadAffinityId()
            );
        }
        if (raw instanceof String rawString) {
            this.context = JsonPath.parse(rawString, config);
        } else if (raw instanceof InputStream rawInputStream) {
            this.context = JsonPath.parse(rawInputStream, config);
        } else if (raw instanceof byte[] rawBytes) {
            try (final ByteArrayInputStream bais = new ByteArrayInputStream(rawBytes)) {
                this.context = JsonPath.parse(bais, config);
            } catch (final IOException e) {
                LOGGER.error("Unable to parse raw bytes into JsonPath instance", e);
            }
        } else {
            this.context = JsonPath.parse(raw, config);
        }
    }

    public <T> T read(final String path,
                      final TypeRef<T> type) {
        return this.context.read(path, type);
    }

}
