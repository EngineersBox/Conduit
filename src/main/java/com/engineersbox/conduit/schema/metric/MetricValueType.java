package com.engineersbox.conduit.schema.metric;

import com.jayway.jsonpath.TypeRef;

public enum MetricValueType {
    STRING(new TypeRef<String>() {}),
    FLOAT(new TypeRef<Float>() {}),
    DOUBLE(new TypeRef<Double>() {}),
    INT(new TypeRef<Integer>() {}),
    BOOLEAN(new TypeRef<Boolean>() {}),
    CONTAINER(null);

    private final TypeRef<?> typeRef;

    MetricValueType(final TypeRef<?> typeRef) {
        this.typeRef = typeRef;
    }

    public TypeRef<?> getTypeRef() {
        return this.typeRef;
    }

}
