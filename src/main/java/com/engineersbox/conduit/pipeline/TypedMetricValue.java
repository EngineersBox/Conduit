package com.engineersbox.conduit.pipeline;

import com.jayway.jsonpath.TypeRef;

public class TypedMetricValue<T> {

    private final TypeRef<T> type;
    private final T value;

    public TypedMetricValue(final T value) {
        this.type = new TypeRef<>() {};
        this.value = value;
    }

    public TypeRef<T> getTypeRef() {
        return this.type;
    }

    public T getValue() {
        return this.value;
    }

}
