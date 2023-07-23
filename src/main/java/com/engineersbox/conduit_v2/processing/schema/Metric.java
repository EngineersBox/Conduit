package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.schema.metric.ParameterizedMetricType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Metric {

    private final String path;
    private final String namespace;
    private final ParameterizedMetricType type;

    @JsonCreator
    public Metric(@JsonProperty("path") final String path,
                  @JsonProperty("namespace") final String namespace,
                  @JsonProperty("type") final ParameterizedMetricType type) {
        this.path = path;
        this.namespace = namespace;
        this.type = type;
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public ParameterizedMetricType getType() {
        return this.type;
    }
}
