package com.engineersbox.conduit_v2.retrieval.content;

import com.engineersbox.conduit_v2.processing.schema.Metric;
import com.engineersbox.conduit_v2.retrieval.ingest.Ingester;
import com.engineersbox.conduit_v2.retrieval.path.PathTraversalHandler;
import io.riemann.riemann.Proto;

import java.util.List;

public class ContentManager<R> {

    private final Ingester<R> ingester = null;
    private final PathTraversalHandler<R> pathTraversalHandler = null;

    public void pollRaw() {
        // TODO: Implement this
        this.ingester.clear();
        this.ingester.consumeSource();
    }

    public List<Proto.Event> retrieveMetricEvents(final Metric metric) {
        final Object rawMetricValue = this.pathTraversalHandler.read(
                metric.getPath(),
                metric.getType().intoConcrete()
        );
        // TODO: Implement this to create event from raw value based on metric definition
        return List.of();
    }



}
