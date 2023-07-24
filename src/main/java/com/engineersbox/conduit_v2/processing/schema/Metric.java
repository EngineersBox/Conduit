package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.schema.metric.ParameterizedMetricType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Metric {

    private final String namespace;
    private final String path;
    private final ParameterizedMetricType structure;

    @JsonCreator
    public Metric(@JsonProperty("namespace") final String namespace,
                  @JsonProperty("path") final String path,
                  @JsonProperty("structure") final ParameterizedMetricType structure) {
        this.namespace = namespace;
        this.path = path;
        this.structure = structure;
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public ParameterizedMetricType getStructure() {
        return this.structure;
    }
}
