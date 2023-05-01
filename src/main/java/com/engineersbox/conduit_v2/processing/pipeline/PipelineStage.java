package com.engineersbox.conduit_v2.processing.pipeline;

import org.apache.commons.lang3.reflect.TypeLiteral;

import java.util.Map;

public abstract class PipelineStage<T, R> implements InvocableStage<T, R> {

    private Map<String, Object> context;
    private final String name;
    protected final Class<T> previousType;
    protected final Class<R> nextType;

    protected PipelineStage(final String name) {
        this.context = null;
        this.name = name;
        this.previousType = (Class<T>) new TypeLiteral<T>(){}.getType();
        this.nextType = (Class<R>) new TypeLiteral<R>(){}.getType();
    }

    public String getName() {
        return this.name;
    }

    void injectContext(final Map<String, Object> context) {
        this.context = context;
    }

    protected Object getContextAttribute(final String key) {
        return this.context.get(key);
    }

    protected void setContextAttribute(final String key, final Object value) {
        this.context.put(key, value);
    }

    @Override
    public abstract R invoke(final T previousResult);

    Object invoke0(final Object previousResult) {
        if (!this.previousType.isInstance(previousResult)) {
            throw new ClassCastException(String.format(
                    "Pipeline stage %s expects %s type for previous result, got %s",
                    this.name,
                    this.previousType.getName(),
                    previousResult.getClass().getName()
            ));
        }
        return invoke(this.previousType.cast(previousResult));
    }

}
