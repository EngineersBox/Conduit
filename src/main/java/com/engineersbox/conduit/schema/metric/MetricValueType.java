package com.engineersbox.conduit.schema.metric;

public enum MetricValueType {
    STRING(String.class),
    FLOAT(float.class),
    DOUBLE(double.class),
    INT(long.class),
    BOOLEAN(boolean.class),
    CONTAINER(null);

    private final Class<?> clazz;

    MetricValueType(final Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getType() {
        return this.clazz;
    }

}
