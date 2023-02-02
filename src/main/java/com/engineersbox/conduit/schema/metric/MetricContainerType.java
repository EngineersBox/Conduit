package com.engineersbox.conduit.schema.metric;

import com.jayway.jsonpath.TypeRef;

import java.util.List;
import java.util.Map;

public enum MetricContainerType {
    MAP(new TypeRef<Map<String, String>>(){}),
    LIST(new TypeRef<List<String>>(){}),
    NONE(null);

    private final TypeRef<?> typeRef;

    MetricContainerType(final TypeRef<?> typeRef) {
        this.typeRef = typeRef;
    }

    public TypeRef<?> getTypeRef() {
        return this.typeRef;
    }

}
