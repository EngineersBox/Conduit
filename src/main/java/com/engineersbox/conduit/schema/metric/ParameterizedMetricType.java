package com.engineersbox.conduit.schema.metric;

import com.engineersbox.conduit.type.ParametrizedTypeConstructor;
import com.jayway.jsonpath.TypeRef;

public class ParameterizedMetricType extends MetricType {

    private final TypeRef<?> concreteType;

    public ParameterizedMetricType(final MetricType child,
                                   final MetricContainerType containerType,
                                   final String suffixFormat) {
        super(child, containerType, suffixFormat);
        this.concreteType = ParametrizedTypeConstructor.construct(this);
    }

    public ParameterizedMetricType(final MetricValueType valueType,
                                   final String suffixFormat) {
        super(valueType, suffixFormat);
        this.concreteType = ParametrizedTypeConstructor.construct(this);
    }

    public ParameterizedMetricType(final MetricType child,
                                   final MetricContainerType containerType,
                                   final MetricValueType valueType,
                                   final String suffixFormat) {
        super(child, containerType, valueType, suffixFormat);
        this.concreteType = ParametrizedTypeConstructor.construct(this);
    }

    public TypeRef<?> intoConcrete() {
        return this.concreteType;
    }

}
