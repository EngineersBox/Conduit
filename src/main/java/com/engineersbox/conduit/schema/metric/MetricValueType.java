package com.engineersbox.conduit.schema.metric;

import io.riemann.riemann.Proto;

import java.util.function.BiFunction;

public enum MetricValueType {
    STRING(
            String.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setState((String) value)
    ),
    FLOAT(
            Float.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setMetricF((float) value)
    ),
    DOUBLE(
            Double.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setMetricD((double) value)
    ),
    INTEGER(
            Long.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setMetricSint64((long) value)
    ),
    BOOLEAN(
            Boolean.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setMetricSint64((boolean) value ? 1 : 0)
    ),
    CONTAINER(null, null);

    private final Class<?> clazz;
    private final BiFunction<Proto.Event.Builder, Object, Proto.Event.Builder> metricBuilder;

    MetricValueType(final Class<?> clazz,
                    final BiFunction<Proto.Event.Builder, Object, Proto.Event.Builder> metricBuilder) {
        this.clazz = clazz;
        this.metricBuilder = metricBuilder;
    }

    public Class<?> getType() {
        return this.clazz;
    }

    public Proto.Event.Builder buildMetric(final Proto.Event.Builder builder,
                                           final Object value) {
        if (this == CONTAINER) {
            throw new UnsupportedOperationException("Cannot build metric for non-primitive type CONTAINER");
        }
        return this.metricBuilder.apply(builder, value);
    }

}
