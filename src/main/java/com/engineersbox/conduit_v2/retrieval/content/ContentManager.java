package com.engineersbox.conduit_v2.retrieval.content;

import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.retrieval.ingest.Ingester;
import com.engineersbox.conduit_v2.retrieval.ingest.IngestionContext;
import com.engineersbox.conduit_v2.retrieval.path.PathTraversalHandler;

public class ContentManager<R> {

    private final Ingester<R> ingester = null;
    private final IngestionContext context = null;
    private final PathTraversalHandler<R> pathTraversalHandler = null;

    public void pollRaw() {
        // TODO: Implement this
        this.ingester.clear();
        this.ingester.consumeSource(this.context);
    }

    public Object retrieveMetricValue(final Metric metric) {
        // TODO: Implement usage of this to create event from raw value based on metric definition. Should this be done in pipeline?
        return this.pathTraversalHandler.read(
                metric.getPath(),
                metric.getType().intoConcrete()
        );
    }



}
