package com.engineersbox.conduit.core.retrieval.content;

import com.engineersbox.conduit.core.schema.metric.Metric;
import com.engineersbox.conduit.core.retrieval.ingest.Ingester;
import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.path.PathTraversalHandler;
import com.jayway.jsonpath.TypeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentManager<T, E extends ConnectorConfiguration, C extends Connector<T, E>> implements RetrievalHandler<Metric> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentManager.class);

    private final Ingester<T, E, C> ingester;
    private final IngestionContext context;
    private final PathTraversalHandler<T> pathTraversalHandler;

    public ContentManager(final Ingester<T, E, C> ingester,
                          final IngestionContext context,
                          final PathTraversalHandler<T> pathTraversalHandler) {
        this.ingester = ingester;
        this.context = context;
        this.pathTraversalHandler = pathTraversalHandler;
    }

    public void poll() throws Exception {
        LOGGER.trace("Polling content manager");
        this.ingester.clear();
        this.ingester.consumeSource(this.context);
        this.pathTraversalHandler.saturate(this.ingester.getCurrent());
        LOGGER.trace("Saturated path traversal handler with currently ingested context");
    }

    @Override
    public Object lookup(final Metric metric) {
        final String path = metric.getPath();
        final TypeRef<?> type = metric.getStructure().intoConcrete();
        LOGGER.trace("Performing look up for metric path {} into type {}", path, type.getType());
        return this.pathTraversalHandler.read(path, type);
    }



}
