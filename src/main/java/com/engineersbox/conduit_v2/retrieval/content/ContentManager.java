package com.engineersbox.conduit_v2.retrieval.content;

import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.retrieval.ingest.Ingester;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.Connector;
import com.engineersbox.conduit_v2.retrieval.ingest.connection.ConnectorConfiguration;
import com.engineersbox.conduit_v2.retrieval.path.PathTraversalHandler;

public class ContentManager<R, E extends ConnectorConfiguration, C extends Connector<R, E>> {

    private final Ingester<R, E, C> ingester = null;
    private final IngestionContext context = null;
    private final PathTraversalHandler<R> pathTraversalHandler = null;

    public void poll() {
        // TODO: Implement this
        this.ingester.clear();
        this.ingester.consumeSource(this.context);
        this.pathTraversalHandler.saturate(this.ingester.getCurrent());
    }

    public Object retrieveMetricValue(final Metric metric) {
        // TODO: Implement usage of this to create event from raw value based on metric definition. Should this be done in pipeline?
        return this.pathTraversalHandler.read(
                metric.getPath(),
                metric.getType().intoConcrete()
        );
    }



}
