package com.engineersbox.conduit.schema.metric;

public enum MetricValueType {
    STRING(String.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),
    INT(Long.class),
    BOOLEAN(Boolean.class),
    CONTAINER(null);

    private final Class<?> clazz;

    MetricValueType(final Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getType() {
        return this.clazz;
    }

}
