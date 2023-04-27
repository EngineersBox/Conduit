package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.schema.metric.ParameterizedMetricType;

public class Metric {

    // TODO: Implement this
    private String path;
    private String metricNamespace;
    private String handlerMethod;
    private ParameterizedMetricType type;

    public String getPath() {
        // TODO: Implement this
        return this.path;
    }

    public ParameterizedMetricType getType() {
        // TODO: Implement this
        return this.type;
    }

}
