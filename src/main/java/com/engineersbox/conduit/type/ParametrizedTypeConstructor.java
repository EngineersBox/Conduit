package com.engineersbox.conduit.type;

import com.engineersbox.conduit.schema.metric.MetricContainerType;
import com.engineersbox.conduit.schema.metric.MetricType;
import com.jayway.jsonpath.TypeRef;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;

public class ParametrizedTypeConstructor {

    public static TypeRef<?> construct(final MetricType metricType) {
        return new TypeRef<>() {

            @Override
            public Type getType() {
                return constructReflectiveType(metricType);
            }

        };
    }

    private static Type constructReflectiveType(final MetricType metricType) {
        if (metricType.isLeaf()) {
            return TypeUtils.wrap(metricType.getValueType().getType()).getType();
        } else if (metricType.getContainerType().equals(MetricContainerType.MAP)) {
            return TypeUtils.parameterize(
                    metricType.getContainerType().getType(),
                    TypeUtils.wrap(String.class).getType(),
                    constructReflectiveType(metricType.getChild().get())
            );
        }
        return TypeUtils.parameterize(
                metricType.getContainerType().getType(),
                constructReflectiveType(metricType.getChild().get())
        );
    }

}
