package com.engineersbox.conduit.processing.pipeline.lua;

import io.riemann.riemann.Proto;
import org.eclipse.collections.api.map.ImmutableMap;

public class EventBatch {

    private final Proto.Event[] events;
    private final ImmutableMap<String, Object> metricExtensions;

    public EventBatch(final Proto.Event[] events,
                      final ImmutableMap<String, Object> metricExtensions) {
        this.events = events;
        this.metricExtensions = metricExtensions;
    }

    public Proto.Event[] getEvents() {
        return this.events;
    }

    public ImmutableMap<String, Object> getMetricExtensions() {
        return this.metricExtensions;
    }

}
