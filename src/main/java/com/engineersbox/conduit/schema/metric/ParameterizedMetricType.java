package com.engineersbox.conduit.schema.metric;

import com.engineersbox.conduit.type.ParametrizedTypeConstructor;
import com.google.common.collect.Range;
import com.jayway.jsonpath.TypeRef;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ParameterizedMetricType extends MetricType {

    private final TypeRef<?> concreteType;

    public ParameterizedMetricType(final MetricType child,
                                   final MetricContainerType containerType,
                                   final List<Pair<Range<Integer>, String>> suffixFormat) {
        super(child, containerType, suffixFormat);
        this.concreteType = ParametrizedTypeConstructor.construct(this);
    }

    public ParameterizedMetricType(final MetricValueType valueType,
                                   final List<Pair<Range<Integer>, String>> suffixFormat) {
        super(valueType, suffixFormat);
        this.concreteType = ParametrizedTypeConstructor.construct(this);
    }

    public ParameterizedMetricType(final MetricType child,
                                   final MetricContainerType containerType,
                                   final MetricValueType valueType,
                                   final List<Pair<Range<Integer>, String>> suffixFormat) {
        super(child, containerType, valueType, suffixFormat);
        this.concreteType = ParametrizedTypeConstructor.construct(this);
    }

    public TypeRef<?> intoConcrete() {
        return this.concreteType;
    }

}
