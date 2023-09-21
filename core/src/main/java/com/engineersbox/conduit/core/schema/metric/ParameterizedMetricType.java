package com.engineersbox.conduit.core.schema.metric;

import com.engineersbox.conduit.core.schema.json.SuffixFormatDeserializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Range;
import com.jayway.jsonpath.TypeRef;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.api.list.ImmutableList;

import javax.annotation.Nullable;

public class ParameterizedMetricType extends MetricType {

    private final TypeRef<?> concreteType;

    @JsonCreator
    public ParameterizedMetricType(@JsonProperty("type") final MetricKind type,
                                   @JsonProperty("structure") @Nullable final ParameterizedMetricType structure,
                                   @JsonProperty("suffixes")
                                   @JsonDeserialize(contentUsing = SuffixFormatDeserializer.class)
                                   @Nullable final ImmutableList<Pair<Range<Integer>, String>> suffixes) {
        super(type, structure, suffixes);
        this.concreteType = ParameterizedTypeConstructor.construct(this);
    }

    public TypeRef<?> intoConcrete() {
        return this.concreteType;
    }

}
