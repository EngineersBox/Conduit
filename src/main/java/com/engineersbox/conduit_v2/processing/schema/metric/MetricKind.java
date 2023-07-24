package com.engineersbox.conduit_v2.processing.schema.metric;

import io.riemann.riemann.Proto;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public enum MetricKind {
    LIST(
            false,
            List.class,
            null
    ),
    MAP(
            false,
            Map.class,
            null
    ),
    STRING(
            true,
            String.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setState((String) value)
    ),
    FLOAT(
            true,
            Float.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setMetricF((float) value)
    ),
    DOUBLE(
            true,
            Double.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setMetricD((double) value)
    ),
    INTEGER(
            true,
            Long.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setMetricSint64((long) value)
    ),
    BOOLEAN(
            true,
            Boolean.class,
            (final Proto.Event.Builder builder, final Object value) -> builder.setMetricSint64((boolean) value ? 1 : 0)
    );

    private final boolean isPrimitive;
    private final Class<?> clazz;
    private final BiFunction<Proto.Event.Builder, Object, Proto.Event.Builder> metricBuilder;

    MetricKind(final boolean isPrimitive,
               final Class<?> clazz,
               final BiFunction<Proto.Event.Builder, Object, Proto.Event.Builder> metricBuilder) {
        this.isPrimitive = isPrimitive;
        this.clazz = clazz;
        this.metricBuilder = metricBuilder;
    }

    public Class<?> getType() {
        return this.clazz;
    }

    public Proto.Event.Builder buildMetric(final Proto.Event.Builder builder,
                                           final Object value) {
        if (!this.isPrimitive) {
            throw new UnsupportedOperationException("Cannot build metric for non-primitive type " + name());
        }
        return this.metricBuilder.apply(builder, value);
    }

}
