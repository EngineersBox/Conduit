package com.engineersbox.conduit.core.retrieval.content;

import com.engineersbox.conduit.core.processing.PollingCondition;
import com.engineersbox.conduit.core.schema.metric.Metric;
import com.engineersbox.conduit.core.retrieval.ingest.Ingester;
import com.engineersbox.conduit.core.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit.core.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit.core.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit.core.retrieval.path.PathTraversalHandler;
import com.jayway.jsonpath.TypeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ContentManager<T, R, E extends ConnectorConfiguration, C extends Connector<T, E>> implements RetrievalHandler<Metric> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentManager.class);

    private final Ingester<T, R, E, C> ingester;
    private final IngestionContext context;
    private final PathTraversalHandler<R> pathTraversalHandler;
    private final PollingCondition pollingCondition;

    public ContentManager(final Ingester<T, R, E, C> ingester,
                          final IngestionContext context,
                          final PathTraversalHandler<R> pathTraversalHandler,
                          final PollingCondition pollingCondition) {
        this.ingester = ingester;
        this.context = context;
        this.pathTraversalHandler = pathTraversalHandler;
        this.pollingCondition = pollingCondition;
    }

    public void setCacheKey(final Optional<String> key) {
        this.ingester.setCacheKey(key);
    }

    public void poll() throws Exception {
        if (this.pollingCondition != PollingCondition.ON_EXECUTE) {
            return;
        }
        poll(null);
    }

    private void poll(final Metric metric) throws Exception {
        LOGGER.trace("Polling content manager");
        this.ingester.clear();
        this.ingester.consumeSource(this.context, metric);
        this.pathTraversalHandler.saturate(
                this.ingester.getCurrent(),
                this.pollingCondition
        );
        LOGGER.trace("Saturated path traversal handler with currently ingested context");
    }

    @Override
    public Object lookup(final Metric metric) throws Exception {
        if (this.pollingCondition == PollingCondition.PER_METRIC) {
            poll(metric);
        }
        final String path = metric.getPath();
        final TypeRef<?> type = metric.getStructure().intoConcrete();
        LOGGER.trace("Performing look up for metric path {} into type {}", path, type.getType());
        return this.pathTraversalHandler.read(
                path,
                type,
                this.pollingCondition
        );
    }



}
