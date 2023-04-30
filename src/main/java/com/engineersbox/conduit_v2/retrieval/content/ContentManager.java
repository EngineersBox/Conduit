package com.engineersbox.conduit_v2.retrieval.content;

import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.retrieval.ingest.Ingester;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit_v2.retrieval.path.PathTraversalHandler;

public class ContentManager<T, R, E extends ConnectorConfiguration, C extends Connector<R, E>> implements RetrievalHandler<Metric> {

    private final Ingester<T, R, E, C> ingester;
    private final IngestionContext context;
    private final PathTraversalHandler<T> pathTraversalHandler;

    public ContentManager(final Ingester<T, R, E, C> ingester,
                          final IngestionContext context,
                          final PathTraversalHandler<T> pathTraversalHandler) {
        this.ingester = ingester;
        this.context = context;
        this.pathTraversalHandler = pathTraversalHandler;
    }

    public void poll() {
        // TODO: Implement this
        this.ingester.clear();
        this.ingester.consumeSource(this.context);
        this.pathTraversalHandler.saturate(this.ingester.getCurrent());
    }

    @Override
    public Object lookup(final Metric metric) {
        // TODO: Implement usage of this to create event from raw value based on metric definition. Should this be done in pipeline?
        return this.pathTraversalHandler.read(
                metric.getPath(),
                metric.getType().intoConcrete()
        );
    }



}
