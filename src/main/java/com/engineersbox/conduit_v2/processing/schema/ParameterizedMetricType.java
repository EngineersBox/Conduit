package com.engineersbox.conduit_v2.processing.schema;

import com.engineersbox.conduit.schema.DimensionallyIndexedRangeMap;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.TypeRef;

import javax.annotation.Nullable;

//@JsonDeserialize(using = MetricTypeDeserializer.class)
public class ParameterizedMetricType extends MetricType {

    private final TypeRef<?> concreteType;

    @JsonCreator
    public ParameterizedMetricType(@JsonProperty("type") final MetricKind type,
                                   @JsonProperty("structure") final ParameterizedMetricType structure,
                                   @JsonProperty("suffixes") final DimensionallyIndexedRangeMap suffixes) {
        super(type, structure, suffixes);
        this.concreteType = ParameterizedTypeConstructor.construct(this);
    }

    public TypeRef<?> intoConcrete() {
        return this.concreteType;
    }

}
