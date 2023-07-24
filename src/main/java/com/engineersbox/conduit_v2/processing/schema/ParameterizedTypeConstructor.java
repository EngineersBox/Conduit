package com.engineersbox.conduit_v2.processing.schema;

import com.jayway.jsonpath.TypeRef;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;

public class ParameterizedTypeConstructor {

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
            return TypeUtils.wrap(metricType.getType().getType()).getType();
        } else if (metricType.getType().equals(MetricKind.MAP)) {
            return TypeUtils.parameterize(
                    metricType.getType().getType(),
                    TypeUtils.wrap(String.class).getType(),
                    constructReflectiveType(metricType.getStructure())
            );
        }
        return TypeUtils.parameterize(
                metricType.getType().getType(),
                constructReflectiveType(metricType.getStructure())
        );
    }

}
