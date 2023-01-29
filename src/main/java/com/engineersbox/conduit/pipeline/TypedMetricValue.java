package com.engineersbox.conduit.pipeline;

import com.google.common.reflect.TypeToken;
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

    public Class<?> getTypeClass() {
        return TypeToken.of(this.type.getType()).getRawType();
    }

    public T getValue() {
        return this.value;
    }

}
