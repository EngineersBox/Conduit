package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.schema.DimensionIndex;
import com.engineersbox.conduit.schema.metric.ParameterizedMetricType;
import org.eclipse.collections.api.map.ImmutableMap;

public class Metric {

    // TODO: Implement this
    private String path;
    private String metricNamespace;
    private String handlerMethod;
    private ImmutableMap<String, String> handlers;
    private ParameterizedMetricType type;

    public String getPath() {
        // TODO: Implement this
        return this.path;
    }

    public ParameterizedMetricType getType() {
        // TODO: Implement this
        return this.type;
    }

    public String getNamespace() {
        return this.metricNamespace;
    }

    public String getSuffix(final DimensionIndex index) {
        return null;
    }

    public ImmutableMap<String, String> getHandlers() {
        return this.handlers;
    }

}
