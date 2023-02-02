package com.engineersbox.conduit.schema.metric;

import com.jayway.jsonpath.TypeRef;

import java.util.List;
import java.util.Map;

public enum MetricContainerType {
    MAP(Map.class),
    LIST(List.class),
    NONE(null);

    private final Class<?> type;

    MetricContainerType(final Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return this.type;
    }

}
