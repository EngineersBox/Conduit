package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.schema.DimensionallyIndexedRangeMap;
import com.engineersbox.conduit_v2.processing.schema.json.MetricTypeDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.annotation.Nullable;

//@JsonDeserialize(using = MetricTypeDeserializer.class)
public class MetricType {

    private final MetricKind type; // Union<ContainerType | ValueType>
    private final ParameterizedMetricType structure; // Child element type structure, nullable
    private final DimensionallyIndexedRangeMap suffixes;

    @JsonCreator
    public MetricType(@JsonProperty("type") final MetricKind type,
                      @JsonProperty("structure") final ParameterizedMetricType structure,
                      @JsonProperty("suffixes") final DimensionallyIndexedRangeMap suffixes) {
        this.type = type;
        this.structure = structure;
        this.suffixes = suffixes;
    }

    public boolean isLeaf() {
        return this.structure == null;
    }

    public MetricKind getType() {
        return this.type;
    }

    public ParameterizedMetricType getStructure() {
        return this.structure;
    }

}
